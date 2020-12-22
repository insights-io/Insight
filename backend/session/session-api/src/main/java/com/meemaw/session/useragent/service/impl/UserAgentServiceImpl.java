package com.meemaw.session.useragent.service.impl;

import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION;

import com.meemaw.session.useragent.service.UserAgentService;
import com.meemaw.useragent.model.DeviceClass;
import com.meemaw.useragent.model.UserAgent;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
public class UserAgentServiceImpl implements UserAgentService {

  private UserAgentAnalyzer uaa;

  @PostConstruct
  public void init() {
    uaa =
        UserAgentAnalyzer.newBuilder()
            .hideMatcherLoadStats()
            .immediateInitialization()
            .withFields(
                DEVICE_NAME,
                DEVICE_BRAND,
                DEVICE_CLASS,
                OPERATING_SYSTEM_NAME,
                OPERATING_SYSTEM_VERSION,
                AGENT_NAME,
                AGENT_VERSION)
            .withCache(10000)
            .build();
  }

  @Override
  @Traced
  public UserAgent parse(String rawUserAgent) {
    nl.basjes.parse.useragent.UserAgent userAgent = uaa.parse(rawUserAgent);
    return new UserAgent(
        userAgent.getValue(DEVICE_NAME),
        userAgent.getValue(DEVICE_BRAND),
        DeviceClass.fromString(userAgent.getValue(DEVICE_CLASS)),
        userAgent.getValue(OPERATING_SYSTEM_NAME),
        userAgent.getValue(OPERATING_SYSTEM_VERSION),
        userAgent.getValue(AGENT_NAME),
        userAgent.getValue(AGENT_VERSION));
  }
}

package com.meemaw.session.useragent.service.impl;

import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME;

import com.meemaw.session.useragent.service.UserAgentService;
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
            .withFields(DEVICE_CLASS, OPERATING_SYSTEM_NAME, AGENT_NAME)
            .withCache(5000)
            .build();
  }

  @Override
  @Traced
  public UserAgent parse(String userAgentString) {
    nl.basjes.parse.useragent.UserAgent userAgent = uaa.parse(userAgentString);
    return new UserAgent(
        userAgent.getValue(DEVICE_CLASS),
        userAgent.getValue(OPERATING_SYSTEM_NAME),
        userAgent.getValue(AGENT_NAME));
  }
}

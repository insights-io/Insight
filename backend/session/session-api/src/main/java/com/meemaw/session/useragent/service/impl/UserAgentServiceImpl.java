package com.meemaw.session.useragent.service.impl;

import static nl.basjes.parse.useragent.UserAgent.*;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.eclipse.microprofile.opentracing.Traced;

import com.meemaw.session.useragent.service.UserAgentService;
import com.meemaw.useragent.model.UserAgentDTO;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

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
  public UserAgentDTO parse(String userAgentString) {
    UserAgent userAgent = uaa.parse(userAgentString);
    return new UserAgentDTO(
        userAgent.getValue(DEVICE_CLASS),
        userAgent.getValue(OPERATING_SYSTEM_NAME),
        userAgent.getValue(AGENT_NAME));
  }
}

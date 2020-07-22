package com.meemaw.session.useragent.service;

import com.meemaw.useragent.model.UserAgentDTO;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
public class UserAgentService {

  /*
  private UserAgentAnalyzer uaa;

  @PostConstruct
  public void init() {
    uaa =
        UserAgentAnalyzer.newBuilder()
            .hideMatcherLoadStats()
            .withFields(
                UserAgent.DEVICE_CLASS, UserAgent.OPERATING_SYSTEM_NAME, UserAgent.AGENT_NAME)
            .withCache(5000)
            .build();
  }
   */

  @Traced
  public UserAgentDTO parse(String userAgentString) {
    return new UserAgentDTO("Desktop", "Mac OS X", "Chrome");
  }
}

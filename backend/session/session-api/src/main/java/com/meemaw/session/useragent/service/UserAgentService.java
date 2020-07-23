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
            .immediateInitialization()
            .withFields(DEVICE_CLASS, OPERATING_SYSTEM_NAME, AGENT_NAME)
            .withCache(5000)
            .build();
  }

  @Traced
  public UserAgentDTO parse(String userAgentString) {
    UserAgent userAgent = uaa.parse(userAgentString);
    return new UserAgentDTO(
        userAgent.getValue(DEVICE_CLASS),
        userAgent.getValue(OPERATING_SYSTEM_NAME),
        userAgent.getValue(AGENT_NAME));
  }
   */

  @Traced
  public UserAgentDTO parse(String userAgentString) {
    return new UserAgentDTO("Desktop", "Mac OS X", "Chrome");
  }
}

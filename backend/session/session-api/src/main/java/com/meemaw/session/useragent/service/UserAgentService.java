package com.meemaw.session.useragent.service;

import com.meemaw.useragent.model.UserAgentDTO;

public interface UserAgentService {

  /**
   * Parse user agent string.
   *
   * @param userAgentString raw string obtained via "User-Agent" header
   * @return parsed user agent
   */
  UserAgentDTO parse(String userAgentString);
}

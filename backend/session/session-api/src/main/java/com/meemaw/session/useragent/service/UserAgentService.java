package com.meemaw.session.useragent.service;

import com.meemaw.useragent.model.UserAgentDTO;

public interface UserAgentService {

  UserAgentDTO parse(String userAgentString);
}

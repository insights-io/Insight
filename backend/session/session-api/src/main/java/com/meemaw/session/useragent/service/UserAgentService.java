package com.meemaw.session.useragent.service;

import com.meemaw.useragent.model.UserAgent;

public interface UserAgentService {

  UserAgent parse(String userAgentString);
}

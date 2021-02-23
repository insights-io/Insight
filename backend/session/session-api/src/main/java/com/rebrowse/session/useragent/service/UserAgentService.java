package com.rebrowse.session.useragent.service;

import com.rebrowse.useragent.model.UserAgent;

public interface UserAgentService {

  UserAgent parse(String rawUserAgent);
}

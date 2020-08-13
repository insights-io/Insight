package com.meemaw.auth.sso.model;

import javax.ws.rs.core.Response;

public interface LoginResult {

  Response response(String cookieDomain);
}

package com.meemaw.auth.sso.model;

import com.meemaw.shared.rest.response.DataResponse;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public interface LoginResult<T> {

  default Response response(String cookieDomain) {
    return DataResponse.okBuilder(getData()).cookie(cookie(cookieDomain)).build();
  }

  T getData();

  NewCookie cookie(String cookieDomain);
}

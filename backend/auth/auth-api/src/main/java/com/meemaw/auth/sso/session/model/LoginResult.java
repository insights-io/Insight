package com.meemaw.auth.sso.session.model;

import com.meemaw.shared.rest.response.DataResponse;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public interface LoginResult<T> {

  default Response loginResponse(String cookieDomain) {
    return DataResponse.okBuilder(getData()).cookie(cookie(cookieDomain)).build();
  }

  default T getData() {
    return null;
  }

  default NewCookie cookie(String cookieDomain) {
    return null;
  }
}

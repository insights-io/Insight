package com.meemaw.auth.sso.model;

import com.meemaw.shared.rest.response.DataResponse;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public interface LoginResult<T> {

  default Response loginResponse(String cookieDomain) {
    return DataResponse.okBuilder(getData()).cookie(cookie(cookieDomain)).build();
  }

  default Response socialLoginResponse(String location, String cookieDomain) {
    return Response.status(Status.FOUND)
        .header("Location", location)
        .cookie(cookie(cookieDomain))
        .build();
  }

  T getData();

  NewCookie cookie(String cookieDomain);
}

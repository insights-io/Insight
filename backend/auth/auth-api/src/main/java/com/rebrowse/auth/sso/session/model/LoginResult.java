package com.rebrowse.auth.sso.session.model;

import com.rebrowse.shared.rest.response.DataResponse;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public interface LoginResult<T> {

  default Response loginResponse(String domain) {
    return loginResponseBuilder(domain).build();
  }

  default Response.ResponseBuilder loginResponseBuilder(String domain) {
    return DataResponse.okBuilder(getData()).cookie(loginCookie(domain));
  }

  default T getData() {
    return null;
  }

  default NewCookie loginCookie(String domain) {
    return null;
  }
}

package com.rebrowse.auth.accounts.model.response;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public interface AuthorizationResponse {

  Response response(NewCookie... cookies);
}

package com.meemaw.auth.sso.service.exception;

import com.meemaw.auth.sso.model.SsoVerification;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import javax.ws.rs.core.Response;

public class VerificationSessionExpiredException extends BoomException {

  public VerificationSessionExpiredException(String message) {
    super(Boom.badRequest().message(message));
  }

  public VerificationSessionExpiredException() {
    this("Verification session expired");
  }

  public Response response(String cookieDomain) {
    return DataResponse.error(getBoom())
        .builder(getBoom().getStatusCode())
        .cookie(SsoVerification.clearCookie(cookieDomain))
        .build();
  }
}

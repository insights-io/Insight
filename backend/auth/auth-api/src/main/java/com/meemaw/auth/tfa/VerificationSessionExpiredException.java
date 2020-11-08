package com.meemaw.auth.tfa;

import com.meemaw.auth.tfa.model.SsoChallenge;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;

import javax.ws.rs.core.Response;

public class VerificationSessionExpiredException extends BoomException {

  public VerificationSessionExpiredException(String message) {
    super(Boom.badRequest().message(message));
  }

  public VerificationSessionExpiredException() {
    this("TFA challenge session expired");
  }

  public Response response(String cookieDomain) {
    return DataResponse.error(getBoom())
        .builder(getBoom().getStatusCode())
        .cookie(SsoChallenge.clearCookie(cookieDomain))
        .build();
  }
}

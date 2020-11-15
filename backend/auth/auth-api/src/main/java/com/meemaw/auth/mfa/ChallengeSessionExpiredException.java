package com.meemaw.auth.mfa;

import com.meemaw.auth.mfa.model.SsoChallenge;
import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import javax.ws.rs.core.Response;

public class ChallengeSessionExpiredException extends BoomException {

  public ChallengeSessionExpiredException(String message) {
    super(Boom.badRequest().message(message));
  }

  public ChallengeSessionExpiredException() {
    this("Challenge session expired");
  }

  public Response response(String cookieDomain) {
    return DataResponse.error(getBoom())
        .builder(getBoom().getStatusCode())
        .cookie(SsoChallenge.clearCookie(cookieDomain))
        .build();
  }
}

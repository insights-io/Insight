package com.rebrowse.auth.mfa;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.shared.rest.exception.BoomException;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
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
        .cookie(AuthorizationPwdChallengeSession.clearCookie(cookieDomain))
        .build();
  }
}

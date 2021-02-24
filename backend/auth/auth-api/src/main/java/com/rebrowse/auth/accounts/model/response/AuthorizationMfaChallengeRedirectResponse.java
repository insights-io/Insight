package com.rebrowse.auth.accounts.model.response;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeSession;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import lombok.Value;
import org.apache.commons.lang3.ArrayUtils;

@Value
public class AuthorizationMfaChallengeRedirectResponse implements AuthorizationResponse {

  URI redirect;
  String challengeId;
  String domain;

  @Override
  public Response response(NewCookie... cookies) {
    return Response.status(302)
        .header(HttpHeaders.LOCATION, redirect)
        .cookie(
            ArrayUtils.add(cookies, AuthorizationMfaChallengeSession.cookie(challengeId, domain)))
        .build();
  }
}

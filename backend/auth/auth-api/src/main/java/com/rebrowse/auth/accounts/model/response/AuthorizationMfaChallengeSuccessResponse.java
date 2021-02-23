package com.rebrowse.auth.accounts.model.response;

import com.rebrowse.auth.accounts.model.AuthorizationSuccessResponseDTO;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeSession;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.shared.rest.response.DataResponse;
import java.net.URI;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import lombok.Value;

@Value
public class AuthorizationMfaChallengeSuccessResponse implements AuthorizationResponse {

  URI location;
  String domain;
  String sessionId;

  @Override
  public Response response(NewCookie... cookies) {
    return DataResponse.okBuilder(new AuthorizationSuccessResponseDTO(location))
        .cookie(
            SsoSession.cookie(sessionId, domain),
            AuthorizationMfaChallengeSession.clearCookie(domain))
        .build();
  }
}

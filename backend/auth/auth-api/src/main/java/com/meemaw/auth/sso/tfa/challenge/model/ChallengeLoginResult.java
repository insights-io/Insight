package com.meemaw.auth.sso.tfa.challenge.model;

import com.meemaw.auth.sso.session.model.LoginResult;
import com.meemaw.auth.sso.tfa.TfaMethod;
import com.meemaw.auth.sso.tfa.challenge.model.dto.ChallengeResponseDTO;
import java.util.List;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.Value;

@Value
public class ChallengeLoginResult implements LoginResult<ChallengeResponseDTO> {

  String challengeId;
  List<TfaMethod> methods;
  String clientCallbackRedirect;

  @Override
  public ChallengeResponseDTO getData() {
    return new ChallengeResponseDTO(challengeId, methods);
  }

  @Override
  public NewCookie cookie(String cookieDomain) {
    return SsoChallenge.cookie(challengeId, cookieDomain);
  }

  @Override
  public Response loginResponse(String cookieDomain) {
    if (clientCallbackRedirect == null) {
      return LoginResult.super.loginResponse(cookieDomain);
    }

    return Response.status(Status.FOUND)
        .header("Location", clientCallbackRedirect)
        .cookie(cookie(cookieDomain))
        .build();
  }
}

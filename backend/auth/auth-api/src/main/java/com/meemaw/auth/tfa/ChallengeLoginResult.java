package com.meemaw.auth.tfa;

import com.meemaw.auth.sso.session.model.LoginResult;
import com.meemaw.auth.tfa.model.SsoChallenge;
import com.meemaw.auth.tfa.model.dto.ChallengeResponseDTO;
import java.net.URL;
import java.util.List;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.Value;

@Value
public class ChallengeLoginResult implements LoginResult<ChallengeResponseDTO> {

  String challengeId;
  // Might be empty if 2fa enforced on organization and user did not set it up yet
  List<TfaMethod> methods;
  URL redirect;

  @Override
  public ChallengeResponseDTO getData() {
    return new ChallengeResponseDTO(challengeId, methods);
  }

  @Override
  public NewCookie cookie(String cookieDomain) {
    return SsoChallenge.cookie(challengeId, cookieDomain);
  }

  @Override
  public Response.ResponseBuilder loginResponseBuilder(String cookieDomain) {
    if (redirect == null) {
      return LoginResult.super.loginResponseBuilder(cookieDomain);
    }
    return Response.status(Status.FOUND).header("Location", redirect).cookie(cookie(cookieDomain));
  }
}

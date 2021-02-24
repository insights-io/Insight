package com.rebrowse.auth.mfa;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeResponseDTO;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.auth.sso.session.model.LoginResult;
import java.net.URI;
import java.util.List;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import lombok.Value;

@Value
public class ChallengeLoginResult implements LoginResult<AuthorizationMfaChallengeResponseDTO> {

  String challengeId;
  // Might be empty if 2fa enforced on organization and user did not set it up yet
  List<MfaMethod> methods;
  URI redirect;

  @Override
  public AuthorizationMfaChallengeResponseDTO getData() {
    return new AuthorizationMfaChallengeResponseDTO(challengeId, methods);
  }

  @Override
  public NewCookie loginCookie(String cookieDomain) {
    return AuthorizationPwdChallengeSession.cookie(challengeId, cookieDomain);
  }

  @Override
  public Response.ResponseBuilder loginResponseBuilder(String cookieDomain) {
    return LoginResult.super.loginResponseBuilder(cookieDomain);
  }
}

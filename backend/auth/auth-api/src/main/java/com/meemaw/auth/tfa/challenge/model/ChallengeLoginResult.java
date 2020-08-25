package com.meemaw.auth.tfa.challenge.model;

import com.meemaw.auth.sso.model.LoginResult;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.challenge.model.dto.ChallengeResponseDTO;
import java.util.List;
import javax.ws.rs.core.NewCookie;
import lombok.Value;

@Value
public class ChallengeLoginResult implements LoginResult<ChallengeResponseDTO> {

  String challengeId;
  List<TfaMethod> methods;

  @Override
  public ChallengeResponseDTO getData() {
    return new ChallengeResponseDTO(challengeId, methods);
  }

  @Override
  public NewCookie cookie(String cookieDomain) {
    return SsoChallenge.cookie(challengeId, cookieDomain);
  }
}

package com.rebrowse.auth.accounts.model.challenge;

import com.rebrowse.auth.accounts.model.AuthorizationAction;
import com.rebrowse.auth.mfa.MfaMethod;
import java.util.List;
import lombok.Value;

@Value
public class AuthorizationMfaChallengeResponseDTO {

  AuthorizationAction action = AuthorizationAction.MFA_CHALLENGE;
  String challengeId;
  List<MfaMethod> methods;
}

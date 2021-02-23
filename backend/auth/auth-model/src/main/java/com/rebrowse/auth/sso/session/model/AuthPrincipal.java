package com.rebrowse.auth.sso.session.model;

import com.rebrowse.auth.user.model.AuthUser;
import javax.enterprise.context.RequestScoped;
import lombok.Data;
import lombok.experimental.Accessors;

@RequestScoped
@Data
@Accessors(fluent = true)
public class AuthPrincipal {

  private AuthUser user;
  private String apiKey;
  private String sessionId;
  private String challengeId;

  boolean isChallengeSession() {
    return challengeId != null;
  }

  boolean isSsoSession() {
    return sessionId != null;
  }

  public String getIdentifier() {
    if (apiKey != null) {
      return apiKey;
    }
    if (sessionId != null) {
      return sessionId;
    }
    if (challengeId != null) {
      return challengeId;
    }
    throw new IllegalStateException("Unauthorized");
  }
}

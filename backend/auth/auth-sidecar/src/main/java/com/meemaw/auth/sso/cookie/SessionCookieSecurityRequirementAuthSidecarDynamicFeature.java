package com.meemaw.auth.sso.cookie;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.rebrowse.model.user.User;
import com.rebrowse.net.RequestOptions;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class SessionCookieSecurityRequirementAuthSidecarDynamicFeature
    extends AbstractSessionCookieSecurityRequirementAuthDynamicFeature {

  @Override
  protected CompletionStage<Optional<AuthUser>> findSession(String sessionId) {
    return User.retrieve(new RequestOptions.Builder().sessionId(sessionId).build())
        .thenApply(
            user ->
                Optional.of(
                    new UserDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        UserRole.fromString(user.getRole().getKey()),
                        user.getOrganizationId(),
                        user.getCreatedAt(),
                        user.getUpdatedAt(),
                        null,
                        user.isPhoneNumberVerified())));
  }
}

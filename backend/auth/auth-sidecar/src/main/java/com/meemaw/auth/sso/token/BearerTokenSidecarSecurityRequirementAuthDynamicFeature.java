package com.meemaw.auth.sso.token;

import com.meemaw.auth.sso.bearer.AbstractBearerTokenSecurityRequirementAuthDynamicFeature;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.rebrowse.model.auth.ApiKey;
import com.rebrowse.net.RequestOptions;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class BearerTokenSidecarSecurityRequirementAuthDynamicFeature
    extends AbstractBearerTokenSecurityRequirementAuthDynamicFeature {

  @Override
  public CompletionStage<Optional<AuthUser>> findUser(String apiKey) {
    return ApiKey.retrieveUser(new RequestOptions.Builder().apiKey(apiKey).build())
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

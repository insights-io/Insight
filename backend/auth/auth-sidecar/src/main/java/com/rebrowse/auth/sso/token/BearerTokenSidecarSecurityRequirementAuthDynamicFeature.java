package com.rebrowse.auth.sso.token;

import com.rebrowse.auth.sso.bearer.AbstractBearerTokenSecurityRequirementAuthDynamicFeature;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.UserRole;
import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.auth.user.model.dto.UserDTO;
import com.rebrowse.model.auth.ApiKey;
import com.rebrowse.net.RequestOptions;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;

@Provider
@ApplicationScoped
public class BearerTokenSidecarSecurityRequirementAuthDynamicFeature
    extends AbstractBearerTokenSecurityRequirementAuthDynamicFeature {

  @ConfigProperty(name = "auth-api/mp-rest/url")
  String authApiBaseUrl;

  @Override
  @Traced
  public CompletionStage<Optional<AuthUser>> findUser(String apiKey) {
    return ApiKey.retrieveUser(
            new RequestOptions.Builder().apiBaseUrl(authApiBaseUrl).apiKey(apiKey).build())
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
                        user.getPhoneNumber() != null
                            ? new PhoneNumberDTO(
                                user.getPhoneNumber().getCountryCode(),
                                user.getPhoneNumber().getDigits())
                            : null,
                        user.isPhoneNumberVerified())));
  }
}

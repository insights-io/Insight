package com.rebrowse.auth.sso.cookie;

import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.UserRole;
import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.auth.user.model.dto.UserDTO;
import com.rebrowse.exception.ApiException;
import com.rebrowse.model.user.User;
import com.rebrowse.net.RequestOptions;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;

@Provider
@ApplicationScoped
public class SsoSessionCookieSecurityRequirementSidecarDynamicFeature
    extends AbstractSsoSessionCookieSecurityRequirementDynamicFeature {

  @ConfigProperty(name = "auth-api/mp-rest/url")
  String authApiBaseUrl;

  @Override
  @Traced
  protected CompletionStage<Optional<AuthUser>> findSession(String cookieValue) {
    return User.retrieve(requestOptions(cookieValue))
        .thenApply(
            user -> {
              AuthUser authUser =
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
                      user.isPhoneNumberVerified());
              return Optional.of(authUser);
            })
        .exceptionally(this::handleError);
  }

  private Optional<AuthUser> handleError(Throwable t) {
    CompletionException completionException = (CompletionException) t;
    if (completionException.getCause() instanceof ApiException) {
      ApiException apiException = (ApiException) t.getCause();
      if (apiException.getStatusCode() == 401) {
        return Optional.empty();
      }
    }
    throw completionException;
  }

  private RequestOptions requestOptions(String sessionId) {
    return new RequestOptions.Builder().apiBaseUrl(authApiBaseUrl).sessionId(sessionId).build();
  }
}

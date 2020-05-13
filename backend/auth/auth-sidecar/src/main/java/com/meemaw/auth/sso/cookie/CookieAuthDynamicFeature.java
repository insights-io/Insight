package com.meemaw.auth.sso.cookie;

import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Provider
@Slf4j
public class CookieAuthDynamicFeature extends AbstractCookieAuthDynamicFeature {

  @Inject @RestClient SsoResource ssoResource;

  @Override
  protected ContainerRequestFilter cookieAuthFilter() {
    return new CookieAuthFilter();
  }

  private class CookieAuthFilter extends AbstractCookieAuthFilter<AuthUser> {

    @Override
    protected CompletionStage<Optional<AuthUser>> findSession(String sessionId) {
      return ssoResource
          .session(sessionId)
          .thenApply(
              response -> {
                int statusCode = response.getStatus();
                if (statusCode == 200) {
                  DataResponse<UserDTO> dataResponse = response.readEntity(new GenericType<>() {});
                  return Optional.of(dataResponse.getData());
                }

                // session not found
                if (statusCode == 204) {
                  return Optional.empty();
                }

                log.error("Unexpected response status {}", statusCode);
                throw Boom.status(statusCode).exception();
              });
    }
  }
}

package com.meemaw.auth.sso.cookie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Provider
@Slf4j
public class CookieAuthDynamicFeature extends AbstractCookieAuthDynamicFeature {

  @Inject @RestClient SsoResource ssoResource;
  @Inject ObjectMapper objectMapper;

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
                if (statusCode == Status.OK.getStatusCode()) {
                  try {
                    DataResponse<UserDTO> dataResponse =
                        objectMapper.readValue(
                            response.readEntity(String.class), new TypeReference<>() {});
                    return Optional.of(dataResponse.getData());
                  } catch (JsonProcessingException ex) {
                    throw Boom.serverError().exception(ex);
                  }
                }

                // session not found
                if (statusCode == Status.NO_CONTENT.getStatusCode()) {
                  return Optional.empty();
                }

                log.error("Unexpected response status {}", statusCode);
                throw Boom.status(statusCode).exception();
              });
    }
  }
}

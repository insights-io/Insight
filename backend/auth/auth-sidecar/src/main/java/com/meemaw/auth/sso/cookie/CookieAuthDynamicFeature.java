package com.meemaw.auth.sso.cookie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.auth.user.resource.v1.UserResource;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Provider
@Slf4j
public class CookieAuthDynamicFeature extends AbstractCookieAuthDynamicFeature {

  @Inject @RestClient UserResource userResource;
  @Inject ObjectMapper objectMapper;

  @Override
  public AbstractCookieAuthFilter authFilter() {
    return new CookieAuthFilter();
  }

  private class CookieAuthFilter extends AbstractCookieAuthFilter {
    @Override
    protected CompletionStage<Optional<AuthUser>> findSession(String sessionId) {
      return userResource
          .me(sessionId)
          .thenApply(
              response -> {
                int statusCode = response.getStatus();
                if (statusCode == Status.OK.getStatusCode()) {
                  try {
                    // TODO: why is this needed? Open issue in Quark  us
                    DataResponse<UserDTO> dataResponse =
                        objectMapper.readValue(
                            response.readEntity(String.class), new TypeReference<>() {});

                    return Optional.of(dataResponse.getData());
                  } catch (JsonProcessingException ex) {
                    log.error("[AUTH]: Failed to parse user", ex);
                    throw Boom.serverError().exception(ex);
                  }
                }

                // session not found
                if (statusCode == Status.NO_CONTENT.getStatusCode()) {
                  log.debug("[AUTH]: Session not found sessionId={}", sessionId);
                  return Optional.empty();
                }

                log.error(
                    "[AUTH]: Failed to find session sessionId={} statusCode={}",
                    sessionId,
                    statusCode);
                throw Boom.serverError().exception();
              });
    }
  }
}

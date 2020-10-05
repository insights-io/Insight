package com.meemaw.auth.sso.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.auth.sso.bearer.AbstractBearerTokenAuthDynamicFeature;
import com.meemaw.auth.sso.token.resource.v1.AuthTokenResource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.dto.UserDTO;
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
public class BearerTokenSidecarAuthDynamicFeature extends AbstractBearerTokenAuthDynamicFeature {

  @Inject @RestClient AuthTokenResource authTokenResource;
  @Inject ObjectMapper objectMapper;

  private String authorizationHeader(String token) {
    return "Bearer " + token;
  }

  @Override
  public CompletionStage<Optional<AuthUser>> findUser(String token) {
    return authTokenResource
        .me(authorizationHeader(token))
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
                  throw Boom.serverError().exception(ex);
                }
              }

              return Optional.empty();
            });
  }
}

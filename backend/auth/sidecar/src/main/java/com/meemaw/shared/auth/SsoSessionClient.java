package com.meemaw.shared.auth;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class SsoSessionClient {

  @ConfigProperty(name = "service.auth.host")
  String AUTH_SERVICE_HOST;

  @ConfigProperty(name = "service.auth.port", defaultValue = "8080")
  int AUTH_SERVICE_PORT;

  @Inject
  private Vertx vertx;

  @Inject
  ObjectMapper objectMapper;

  private WebClient webClient;

  @PostConstruct
  private void initialize() {
    webClient = WebClient.create(vertx);
  }

  public Uni<Optional<AuthUser>> session(String sessionId) {
    return webClient.get(AUTH_SERVICE_PORT, AUTH_SERVICE_HOST, "/v1/sso/session")
        .addQueryParam("id", sessionId)
        .send()
        .map(response -> {
          int statusCode = response.statusCode();
          if (statusCode == 200) {
            try {
              DataResponse<UserDTO> dataResponse = objectMapper
                  .readValue(response.bodyAsString(), new TypeReference<>() {
                  });
              return Optional.of(dataResponse.getData());
            } catch (JsonProcessingException ex) {
              log.error("Failed to parse SsoSession", ex);
              throw Boom.serverError().exception();
            }
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

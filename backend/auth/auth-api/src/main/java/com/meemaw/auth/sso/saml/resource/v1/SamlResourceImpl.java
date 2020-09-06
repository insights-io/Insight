package com.meemaw.auth.sso.saml.resource.v1;

import com.meemaw.auth.sso.saml.model.SamlDataResponse;
import com.meemaw.auth.sso.saml.model.SamlMetadataResponse;
import com.meemaw.auth.sso.saml.service.SamlServiceImpl;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.http.HttpServerRequest;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.xml.sax.SAXException;

@Slf4j
public class SamlResourceImpl implements SamlResource {

  @Context UriInfo info;
  @Context HttpServerRequest request;
  @Inject SamlServiceImpl samlService;
  @Inject SsoService ssoService;

  private static final String METADATA =
      "https://snuderls.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata";

  public void init(@Observes StartupEvent event) {
    try {
      InitializationService.initialize();
    } catch (InitializationException ex) {
      log.error("Failed to initialize SAML", ex);
    }
  }

  @Override
  public Response signIn(String destination) {
    log.info("[AUTH]: SAML signIn request destination={}", destination);
    String refererBaseURL =
        RequestUtils.parseRefererBaseURL(request)
            .orElseThrow(() -> Boom.badRequest().message("referer required").exception());

    try {
      SamlMetadataResponse metadata = samlService.fetchMetadata(new URL(METADATA));
      String relayState = samlService.secureState(refererBaseURL + destination);
      String location = samlService.buildAuthorizationUri(metadata, relayState);
      return Response.status(Status.FOUND)
          .cookie(new NewCookie("state", relayState))
          .header("Location", location)
          .build();
    } catch (IOException | ParserConfigurationException | SAXException ex) {
      log.error("[AUTH]: SAML signIn failed to start flow", ex);
      return Boom.serverError().response();
    }
  }

  @Override
  public CompletionStage<Response> callback(
      String SAMLResponse, String relayState, String sessionState) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(relayState)) {
      log.warn("[AUTH]: SAML state miss-match, expected={}, actual={}", relayState, sessionState);
      throw Boom.status(Status.UNAUTHORIZED).message("Invalid state parameter").exception();
    }

    SamlDataResponse samlDataResponse;
    try {
      samlDataResponse = samlService.decodeSamlResponse(SAMLResponse);
    } catch (ParserConfigurationException
        | IOException
        | SAXException
        | UnmarshallingException ex) {
      log.error("[AUTH]: Failed to decode SAMLResponse={}", SAMLResponse, ex);
      return CompletableFuture.completedStage(
          Boom.badRequest().message("Invalid SAMLResponse").response());
    }

    if (samlDataResponse.getSignature() == null) {
      log.error("[AUTH]: SAML callback missing signature SAMLResponse={}", SAMLResponse);
      return CompletableFuture.completedStage(
          Boom.badRequest().message("Missing signature").response());
    }

    SamlMetadataResponse samlMetadata;
    try {
      samlMetadata = samlService.fetchMetadata(new URL(METADATA));
    } catch (IOException | ParserConfigurationException | SAXException ex) {
      log.error("[AUTH]: SAML callback failed to fetch metadata url={}", METADATA);
      return CompletableFuture.completedStage(Boom.serverError().response());
    }

    if (!samlMetadata.getEntityId().equals(samlDataResponse.getIssuer())) {
      log.error(
          "[AUTH]: SAML callback entity miss-match expected={} actual={}",
          samlMetadata.getEntityId(),
          samlDataResponse.getIssuer());
      return CompletableFuture.completedStage(
          Boom.badRequest().message("Invalid entityId").response());
    }

    samlService.validateSignature(samlDataResponse.getSignature(), samlMetadata);

    String email = samlDataResponse.getEmail();
    String serverBaseURL = RequestUtils.getServerBaseURL(info, request);
    String cookieDomain = RequestUtils.parseCookieDomain(serverBaseURL);
    String location = samlService.secureStateData(relayState);
    return ssoService
        .socialLogin(email, samlDataResponse.getFullName())
        .thenApply(
            loginResult -> {
              log.info(
                  "[AUTH]: SAML callback successfully authenticated user email={} location={}",
                  email,
                  location);

              return loginResult.socialLoginResponse(location, cookieDomain);
            });
  }
}

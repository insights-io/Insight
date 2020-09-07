package com.meemaw.auth.sso.saml.resource.v1;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.saml.model.SamlDataResponse;
import com.meemaw.auth.sso.saml.model.SamlMetadataResponse;
import com.meemaw.auth.sso.saml.service.SamlServiceImpl;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import io.vertx.core.http.HttpServerRequest;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.opensaml.core.xml.io.UnmarshallingException;

@Slf4j
public class SamlResourceImpl implements SamlResource {

  @Context UriInfo info;
  @Context HttpServerRequest request;
  @Inject SamlServiceImpl samlService;
  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SsoService ssoService;

  @Override
  public CompletionStage<Response> signIn(String redirect, String email) {
    String refererBaseURL =
        RequestUtils.parseRefererBaseURL(request)
            .orElseThrow(() -> Boom.badRequest().message("referer required").exception());
    String refererCallback = refererBaseURL + redirect;
    log.info("[AUTH]: SAML signIn request email={} refererCallback={}", email, refererCallback);
    String domain = EmailUtils.domainFromEmail(email);

    return ssoSetupDatasource
        .getByDomain(domain)
        .thenApply(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isEmpty()) {
                log.info("[AUTH]: SSO not configured for email={}", email);
                return Boom.badRequest()
                    .message("SSO not configured. Please contact your administrator.")
                    .response();
              }

              String configurationEndpoint = maybeSsoSetup.get().getConfigurationEndpoint();
              try {
                SamlMetadataResponse metadata =
                    samlService.fetchMetadata(new URL(configurationEndpoint));
                String relayState = samlService.secureState(refererCallback);
                String location = samlService.buildAuthorizationUri(metadata, relayState);
                return Response.status(Status.FOUND)
                    .cookie(new NewCookie("state", relayState))
                    .header("Location", location)
                    .build();
              } catch (IOException | XMLParserException ex) {
                log.error("[AUTH]: SAML signIn failed to start flow", ex);
                return Boom.serverError().response();
              }
            });
  }

  @Override
  public CompletionStage<Response> callback(
      String samlResponse, String relayState, String sessionState) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(relayState)) {
      log.warn("[AUTH]: SAML state miss-match, expected={}, actual={}", relayState, sessionState);
      throw Boom.status(Status.UNAUTHORIZED).message("Invalid state parameter").exception();
    }

    SamlDataResponse samlDataResponse;
    try {
      samlDataResponse = samlService.decodeSamlResponse(samlResponse);
    } catch (UnmarshallingException | XMLParserException ex) {
      log.error("[AUTH]: Failed to decode SAMLResponse={}", samlResponse, ex);
      return CompletableFuture.completedStage(
          Boom.badRequest().message("Invalid SAMLResponse").response());
    }

    if (samlDataResponse.getSignature() == null) {
      log.error("[AUTH]: SAML callback missing signature SAMLResponse={}", samlResponse);
      return CompletableFuture.completedStage(
          Boom.badRequest().message("Missing signature").response());
    }

    String email = samlDataResponse.getEmail();
    String domain = EmailUtils.domainFromEmail(email);

    return ssoSetupDatasource
        .getByDomain(domain)
        .thenCompose(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isEmpty()) {
                log.info("[AUTH]: SSO not configured for email={}", email);
                return CompletableFuture.completedStage(
                    Boom.badRequest()
                        .message("SSO not configured. Please contact your administrator.")
                        .response());
              }
              String configurationEndpoint = maybeSsoSetup.get().getConfigurationEndpoint();
              SamlMetadataResponse samlMetadata;
              try {
                samlMetadata = samlService.fetchMetadata(new URL(configurationEndpoint));
              } catch (IOException | XMLParserException ex) {
                log.error(
                    "[AUTH]: SAML callback failed to fetch metadata configurationEndpoint={}",
                    configurationEndpoint);
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
            });
  }
}

package com.rebrowse.auth.sso.saml.resource.v1;

import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.core.EmailUtils;
import com.rebrowse.auth.sso.saml.service.SamlService;
import com.rebrowse.auth.sso.setup.datasource.SsoSetupDatasource;
import com.rebrowse.auth.sso.setup.model.dto.SsoSetupDTO;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.shared.context.URIUtils;
import com.rebrowse.shared.rest.response.Boom;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SamlResourceImpl implements SamlResource {

  @Inject SamlService samlService;
  @Inject SsoSetupDatasource ssoSetupDatasource;

  @Context UriInfo info;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> signIn(String email, URL redirect) {
    String emailDomain = EmailUtils.getDomain(email);
    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    URI redirectUri = RequestUtils.sneakyUri(redirect);

    return ssoSetupDatasource
        .getByDomain(emailDomain)
        .thenCompose(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isEmpty()) {
                return CompletableFuture.completedStage(
                    Boom.badRequest()
                        .message("That email or domain isn’t registered for SSO.")
                        .response());
              }

              SsoSetupDTO ssoSetup = maybeSsoSetup.get();
              AuthorizationRequest authorizationRequest =
                  new AuthorizationRequest(email, redirectUri, serverBaseUri);

              return samlService
                  .getSsoAuthorizationRedirectResponse(ssoSetup, authorizationRequest)
                  .thenApply(AuthorizationResponse::response);
            });
  }

  @Override
  public CompletionStage<Response> callback(
      String samlResponse, String relayState, String sessionState) {
    if (!Optional.ofNullable(sessionState).orElse("").equals(relayState)) {
      log.debug("[AUTH]: SAML state miss-match, expected={}, actual={}", relayState, sessionState);
      return CompletableFuture.completedStage(
          Boom.status(Status.UNAUTHORIZED).message("Invalid state parameter").response());
    }

    URI serverBaseUri = RequestUtils.getServerBaseUri(info, request);
    String domain = URIUtils.parseCookieDomain(serverBaseUri);

    return samlService
        .handleCallback(samlResponse, relayState, serverBaseUri)
        .thenApply(auth -> auth.response(SsoAuthorizationSession.clearCookie(domain)));
  }
}

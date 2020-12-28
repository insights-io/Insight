package com.meemaw.auth.sso.saml.resource.v1;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.saml.service.SamlService;
import com.meemaw.auth.sso.session.model.SsoLoginResult;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import io.vertx.core.http.HttpServerRequest;
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

  @Context UriInfo info;
  @Context HttpServerRequest request;
  @Inject SamlService samlService;
  @Inject SsoSetupDatasource ssoSetupDatasource;

  @Override
  public CompletionStage<Response> signIn(URL redirect, String email) {
    log.info("[AUTH]: SAML signIn request email={} redirect={}", email, redirect);
    String domain = EmailUtils.domainFromEmail(email);
    String cookieDomain =
        RequestUtils.parseCookieDomain(RequestUtils.getServerBaseUri(info, request));

    return ssoSetupDatasource
        .getByDomain(domain)
        .thenApply(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isEmpty()) {
                log.info("[AUTH]: SSO not configured for email={}", email);
                return Boom.badRequest()
                    .message("That email or domain isn’t registered for SSO.")
                    .response();
              }

              URL configurationEndpoint = maybeSsoSetup.get().getSaml().getMetadataEndpoint();
              return samlService.signInRedirectResponse(
                  configurationEndpoint, cookieDomain, redirect);
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

    return samlService.handleCallback(samlResponse, relayState).thenApply(SsoLoginResult::response);
  }
}

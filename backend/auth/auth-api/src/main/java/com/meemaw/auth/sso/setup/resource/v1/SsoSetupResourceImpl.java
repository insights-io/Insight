package com.meemaw.auth.sso.setup.resource.v1;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.IdentityProviderRegistry;
import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.dto.CreateSsoSetupDTO;
import com.meemaw.auth.sso.setup.service.SsoSetupService;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsoSetupResourceImpl implements SsoSetupResource {

  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SsoSetupService ssoSetupService;
  @Inject InsightPrincipal insightPrincipal;
  @Inject IdentityProviderRegistry identityProviderRegistry;
  @Context UriInfo info;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> create(CreateSsoSetupDTO body) {
    AuthUser user = insightPrincipal.user();
    SsoMethod method = body.getMethod();
    URL configurationEndpoint = body.getConfigurationEndpoint();
    if (SsoMethod.SAML.equals(method) && configurationEndpoint == null) {
      log.info(
          "[AUTH]: Trying to setup SAML SSO without configuration endpoint email={}",
          user.getEmail());

      throw Boom.validationErrors(Map.of("configurationEndpoint", "Required")).exception();
    }

    String organizationId = user.getOrganizationId();
    String domain = EmailUtils.domainFromEmail(user.getEmail());
    log.info(
        "[AUTH]: SSO setup request organization={} domain={} method={} configurationEndpoint={}",
        organizationId,
        domain,
        method,
        configurationEndpoint);

    return ssoSetupService.setup(organizationId, domain, body).thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> get() {
    String organizationId = insightPrincipal.user().getOrganizationId();
    log.info("[AUTH] SSO setup get request organization={}", organizationId);
    return ssoSetupDatasource
        .get(organizationId)
        .thenApply(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isEmpty()) {
                return Boom.notFound()
                    .message("That email or domain isn’t registered for SSO.")
                    .response();
              }
              return DataResponse.ok(maybeSsoSetup.get());
            });
  }

  @Override
  public CompletionStage<Response> get(String domain) {
    log.info("[AUTH] SSO setup get request domain={}", domain);
    URI serverBaseURI = RequestUtils.getServerBaseURI(info, request);

    return ssoSetupDatasource
        .getByDomain(domain)
        .thenApply(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isEmpty()) {
                return DataResponse.ok(false);
              }

              return DataResponse.ok(
                  identityProviderRegistry.ssoSignInLocationBase(
                      maybeSsoSetup.get().getMethod(), serverBaseURI));
            });
  }
}

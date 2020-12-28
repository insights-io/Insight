package com.meemaw.auth.sso.setup.resource.v1;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.IdentityProviderRegistry;
import com.meemaw.auth.sso.session.model.AuthPrincipal;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.dto.CreateSsoSetupParams;
import com.meemaw.auth.sso.setup.model.dto.SamlConfiguration;
import com.meemaw.auth.sso.setup.service.SsoSetupService;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsoSetupResourceImpl implements SsoSetupResource {

  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SsoSetupService ssoSetupService;
  @Inject AuthPrincipal authPrincipal;
  @Inject IdentityProviderRegistry identityProviderRegistry;
  @Context UriInfo info;
  @Context HttpServerRequest request;
  @Inject Validator validator;

  @Override
  public CompletionStage<Response> create(CreateSsoSetupParams body) {
    AuthUser user = authPrincipal.user();
    SsoMethod method = body.getMethod();

    if (SsoMethod.SAML.equals(method)) {
      SamlConfiguration samlConfiguration =
          Optional.ofNullable(body.getSaml())
              .orElseThrow(() -> Boom.badRequest().errors(Map.of("saml", "Required")).exception());

      Set<ConstraintViolation<SamlConfiguration>> constraintViolations =
          validator.validate(samlConfiguration);

      if (!constraintViolations.isEmpty()) {
        throw new ConstraintViolationException(constraintViolations);
      }
    }

    String organizationId = user.getOrganizationId();
    String domain = EmailUtils.domainFromEmail(user.getEmail());
    log.info(
        "[AUTH]: SSO setup request organization={} domain={} method={}",
        organizationId,
        domain,
        method);

    return ssoSetupService.setup(organizationId, domain, body).thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> get() {
    String organizationId = authPrincipal.user().getOrganizationId();
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
  public CompletionStage<Response> delete() {
    String organizationId = authPrincipal.user().getOrganizationId();

    log.info("[AUTH] SSO setup delete request organizationId={}", organizationId);
    return ssoSetupDatasource
        .delete(organizationId)
        .thenApply(
            deleted -> {
              if (deleted) {
                return Response.noContent().build();
              }
              return Boom.notFound().response();
            });
  }

  @Override
  public CompletionStage<Response> get(String domain) {
    log.info("[AUTH] SSO setup get request domain={}", domain);
    URI serverBaseURI = RequestUtils.getServerBaseUri(info, request);

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

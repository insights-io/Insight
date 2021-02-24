package com.rebrowse.auth.sso.setup.resource.v1;

import com.rebrowse.auth.core.EmailUtils;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.sso.setup.datasource.SsoSetupDatasource;
import com.rebrowse.auth.sso.setup.model.SsoMethod;
import com.rebrowse.auth.sso.setup.model.dto.CreateSsoSetupParams;
import com.rebrowse.auth.sso.setup.model.dto.SamlConfigurationDTO;
import com.rebrowse.auth.sso.setup.service.SsoSetupService;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
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

  @Inject Validator validator;
  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SsoSetupService ssoSetupService;
  @Inject AuthPrincipal authPrincipal;

  @Context UriInfo info;
  @Context HttpServerRequest request;

  @Override
  public CompletionStage<Response> create(CreateSsoSetupParams body) {
    AuthUser user = authPrincipal.user();
    SsoMethod method = body.getMethod();

    if (SsoMethod.SAML.equals(method)) {
      SamlConfigurationDTO samlConfiguration =
          Optional.ofNullable(body.getSaml())
              .orElseThrow(() -> Boom.badRequest().errors(Map.of("saml", "Required")).exception());

      Set<ConstraintViolation<SamlConfigurationDTO>> constraintViolations =
          validator.validate(samlConfiguration);

      if (!constraintViolations.isEmpty()) {
        throw new ConstraintViolationException(constraintViolations);
      }
    }

    String organizationId = user.getOrganizationId();
    String domain = EmailUtils.getDomain(user.getEmail());
    log.info(
        "[AUTH]: SSO setup request organization={} domain={} method={}",
        organizationId,
        domain,
        method);

    return ssoSetupService.setup(organizationId, domain, body).thenApply(DataResponse::created);
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
  public CompletionStage<Response> get() {
    String organizationId = authPrincipal.user().getOrganizationId();
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
}

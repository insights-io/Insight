package com.meemaw.auth.sso.setup.resource.v1;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.auth.sso.saml.service.SamlServiceImpl;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.setup.model.CreateSsoSetupDTO;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsoSetupResourceImpl implements SsoSetupResource {

  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SamlServiceImpl samlService;
  @Inject InsightPrincipal insightPrincipal;

  @Override
  public CompletionStage<Response> setup(CreateSsoSetupDTO body) {
    SsoMethod method = body.getMethod();
    URL configurationEndpoint = body.getConfigurationEndpoint();
    String organizationId = insightPrincipal.user().getOrganizationId();
    String domain = EmailUtils.domainFromEmail(insightPrincipal.user().getEmail());
    log.info(
        "[AUTH]: SSO setup request organization={} domain={} method={} configurationEndpoint={}",
        organizationId,
        domain,
        method,
        configurationEndpoint);

    return samlService
        .setupSamlSso(organizationId, domain, configurationEndpoint)
        .thenApply(DataResponse::created);
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
    return ssoSetupDatasource
        .getByDomain(domain)
        .thenApply(maybeSsoSetup -> DataResponse.ok(maybeSsoSetup.isPresent()));
  }
}

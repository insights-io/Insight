package com.meemaw.auth.sso.setup.resource.v1;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.auth.sso.saml.service.SamlServiceImpl;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.setup.model.CreateSsoSetupDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

@Slf4j
public class SsoSetupResourceImpl implements SsoSetupResource {

  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SamlServiceImpl samlService;
  @Inject InsightPrincipal insightPrincipal;

  @Override
  public CompletionStage<Response> setup(String configurationEndpoint) {
    String organizationId = insightPrincipal.user().getOrganizationId();
    String domain = EmailUtils.domainFromEmail(insightPrincipal.user().getEmail());
    log.info(
        "[AUTH]: SSO setup request for organization={} configurationEndpoint={}",
        organizationId,
        configurationEndpoint);

    URL url;
    try {
      url = new URL(configurationEndpoint);
    } catch (MalformedURLException ex) {
      log.error(
          "[AUTH]: Malformed SSO setup request organization={} configurationEndpoint={}",
          organizationId,
          configurationEndpoint,
          ex);
      return CompletableFuture.completedStage(
          Boom.badRequest().errors(Map.of("configurationEndpoint", ex.getMessage())).response());
    }

    return ssoSetupDatasource
        .get(organizationId)
        .thenCompose(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isPresent()) {
                log.info("[AUTH]: SSO setup already configured organization={}", organizationId);
                return CompletableFuture.completedStage(
                    Boom.badRequest().message("SSO setup already configured").response());
              }

              try {
                // TODO: validate response
                samlService.fetchMetadata(url);
                CreateSsoSetupDTO createSsoSetup =
                    new CreateSsoSetupDTO(organizationId, domain, "saml", configurationEndpoint);

                return ssoSetupDatasource
                    .create(createSsoSetup)
                    .thenApply(ssoSetup -> Response.status(201).entity(ssoSetup).build());
              } catch (IOException | XMLParserException ex) {
                log.error(
                    "[AUTH]: Failed to fetch SSO organizationId={} configurationEndpoint={}",
                    organizationId,
                    configurationEndpoint,
                    ex);
                return CompletableFuture.completedStage(
                    Boom.badRequest().message("Failed to fetch SSO configuration").response());
              }
            });
  }

  @Override
  public CompletionStage<Response> get(String domain) {
    log.info("[AUTH] SSO setup get request domain={}", domain);
    return ssoSetupDatasource
        .getByDomain(domain)
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

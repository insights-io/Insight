package com.meemaw.auth.sso.setup.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.saml.service.SamlService;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.setup.model.CreateSsoSetup;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.dto.CreateSsoSetupParams;
import com.meemaw.auth.sso.setup.model.dto.SsoSetup;
import com.meemaw.shared.rest.response.Boom;

import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Slf4j
public class SsoSetupService {

  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SamlService samlService;

  @Traced
  public CompletionStage<SsoSetup> setup(
      String organizationId, String domain, CreateSsoSetupParams params) {
    SsoMethod method = params.getMethod();

    if (!EmailUtils.isBusinessDomain(domain)) {
      log.info(
          "[AUTH]: Tried to setup SSO on non work email organization={} domain={}",
          organizationId,
          domain);
      throw Boom.badRequest().message("SSO setup is only possible for work domain.").exception();
    }

    return ssoSetupDatasource
        .get(organizationId)
        .thenCompose(
            maybeSsoSetup -> {
              if (maybeSsoSetup.isPresent()) {
                log.info("[AUTH]: SSO setup already configured organization={}", organizationId);
                throw Boom.badRequest().message("SSO setup already configured").exception();
              }

              if (method.equals(SsoMethod.SAML)) {
                URL metadataEndpoint = params.getSaml().getMetadataEndpoint();
                samlService.validateConfigurationEndpoint(metadataEndpoint);
              }

              return ssoSetupDatasource
                  .create(new CreateSsoSetup(organizationId, domain, method, params.getSaml()))
                  .thenApply(
                      ssoSetup -> {
                        log.info(
                            "[AUTH]: SSO setup successful method={} organization={}",
                            method,
                            organizationId);
                        return ssoSetup;
                      });
            });
  }
}

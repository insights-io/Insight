package com.meemaw.auth.sso.setup.service;

import com.meemaw.auth.core.EmailUtils;
import com.meemaw.auth.sso.saml.service.SamlServiceImpl;
import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.setup.model.CreateSsoSetup;
import com.meemaw.auth.sso.setup.model.CreateSsoSetupDTO;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.SsoSetupDTO;
import com.meemaw.shared.rest.response.Boom;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class SsoSetupService {

  @Inject SsoSetupDatasource ssoSetupDatasource;
  @Inject SamlServiceImpl samlService;

  public CompletionStage<SsoSetupDTO> setup(
      String organizationId, String domain, CreateSsoSetupDTO createSsoSetupDTO) {
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

              SsoMethod method = createSsoSetupDTO.getMethod();
              URL configurationEndpoint = createSsoSetupDTO.getConfigurationEndpoint();
              if (SsoMethod.SAML.equals(method)) {
                samlService.validateConfigurationEndpoint(configurationEndpoint);
              }
              CreateSsoSetup createSsoSetup =
                  new CreateSsoSetup(organizationId, domain, SsoMethod.SAML, configurationEndpoint);
              return ssoSetupDatasource.create(createSsoSetup);
            });
  }
}

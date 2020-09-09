package com.meemaw.auth.sso;

import com.meemaw.auth.sso.saml.service.SamlServiceImpl;
import com.meemaw.auth.sso.setup.model.SsoSetupDTO;
import java.net.URL;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class IdpServiceRegistry {

  @Inject SamlServiceImpl samlService;

  public Response signInRedirectResponse(String clientCallbackRedirect, SsoSetupDTO ssoSetup) {
    URL configurationEndpoint = ssoSetup.getConfigurationEndpoint();
    return samlService.signInRedirectResponse(clientCallbackRedirect, configurationEndpoint);
  }
}

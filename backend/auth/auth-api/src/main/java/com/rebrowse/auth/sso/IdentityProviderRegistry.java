package com.rebrowse.auth.sso;

import com.rebrowse.auth.sso.oauth.github.GithubIdentityProvider;
import com.rebrowse.auth.sso.oauth.google.GoogleIdentityProvider;
import com.rebrowse.auth.sso.oauth.microsoft.MicrosoftIdentityProvider;
import com.rebrowse.auth.sso.saml.service.SamlService;
import com.rebrowse.auth.sso.setup.model.SsoMethod;
import io.quarkus.runtime.StartupEvent;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class IdentityProviderRegistry {

  @Inject MicrosoftIdentityProvider microsoftService;
  @Inject GoogleIdentityProvider googleService;
  @Inject GithubIdentityProvider githubService;
  @Inject SamlService samlService;

  private Map<SsoMethod, AbstractIdentityProvider> services;

  public void init(@Observes StartupEvent event) {
    services = new HashMap<>();
    services.put(SsoMethod.MICROSOFT, microsoftService);
    services.put(SsoMethod.GOOGLE, googleService);
    services.put(SsoMethod.GITHUB, githubService);
    services.put(SsoMethod.SAML, samlService);
  }

  public AbstractIdentityProvider getService(SsoMethod ssoMethod) {
    return services.get(ssoMethod);
  }
}

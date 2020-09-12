package com.meemaw.auth.sso;

import com.meemaw.auth.sso.oauth.github.OAuth2GithubService;
import com.meemaw.auth.sso.oauth.google.OAuth2GoogleService;
import com.meemaw.auth.sso.oauth.microsoft.OAuth2MicrosoftService;
import com.meemaw.auth.sso.saml.service.SamlServiceImpl;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.SsoSetupDTO;
import io.quarkus.runtime.StartupEvent;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@ApplicationScoped
public class IdpServiceRegistry {

  @Inject OAuth2MicrosoftService microsoftService;
  @Inject OAuth2GoogleService oAuth2GoogleService;
  @Inject OAuth2GithubService oAuth2GithubService;
  @Inject SamlServiceImpl samlService;

  private Map<SsoMethod, AbstractIdpService> services;

  public void init(@Observes StartupEvent event) {
    services = new HashMap<>();
    services.put(SsoMethod.MICROSOFT, microsoftService);
    services.put(SsoMethod.GOOGLE, oAuth2GoogleService);
    services.put(SsoMethod.GITHUB, oAuth2GithubService);
    services.put(SsoMethod.SAML, samlService);
  }

  public AbstractIdpService getService(SsoMethod ssoMethod) {
    return services.get(ssoMethod);
  }

  public Function<String, Response> ssoSignInRedirect(
      SsoSetupDTO setup, String clientCallback, String serverBaseURL) {
    AbstractIdpService idpService = getService(setup.getMethod());
    String serverRedirect = serverBaseURL + idpService.callbackPath();
    String state = idpService.secureState(clientCallback);
    URI location = idpService.buildAuthorizationURI(state, serverRedirect, setup);

    return (cookieDomain) ->
        Response.status(Status.FOUND)
            .cookie(SsoSignInSession.cookie(state))
            .header("Location", location)
            .build();
  }
}

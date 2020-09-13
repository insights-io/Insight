package com.meemaw.auth.sso;

import com.meemaw.auth.sso.oauth.github.OAuth2GithubService;
import com.meemaw.auth.sso.oauth.google.OAuth2GoogleService;
import com.meemaw.auth.sso.oauth.microsoft.OAuth2MicrosoftService;
import com.meemaw.auth.sso.saml.service.SamlServiceImpl;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.SsoSetupDTO;
import io.quarkus.runtime.StartupEvent;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

@ApplicationScoped
public class IdpServiceRegistry {

  @Inject OAuth2MicrosoftService microsoftService;
  @Inject OAuth2GoogleService googleService;
  @Inject OAuth2GithubService githubService;
  @Inject SamlServiceImpl samlService;

  private Map<SsoMethod, AbstractIdpService> services;

  public void init(@Observes StartupEvent event) {
    services = new HashMap<>();
    services.put(SsoMethod.MICROSOFT, microsoftService);
    services.put(SsoMethod.GOOGLE, googleService);
    services.put(SsoMethod.GITHUB, githubService);
    services.put(SsoMethod.SAML, samlService);
  }

  public AbstractIdpService getService(SsoMethod ssoMethod) {
    return services.get(ssoMethod);
  }

  public URI ssoSignInLocation(SsoMethod method, URI serverBaseURI) {
    AbstractIdpService idpService = getService(method);
    return UriBuilder.fromUri(serverBaseURI).path(idpService.signInPath()).build();
  }

  public Function<String, Response> ssoSignInRedirect(
      String email, SsoSetupDTO setup, URL redirect, URI serverBaseURI) {
    URI ssoSignInLocation = ssoSignInLocation(setup.getMethod(), serverBaseURI);
    URI location =
        UriBuilder.fromUri(ssoSignInLocation)
            .queryParam("redirect", redirect)
            .queryParam("email", email)
            .build();

    return (cookieDomain) -> Response.status(Status.FOUND).header("Location", location).build();
  }
}

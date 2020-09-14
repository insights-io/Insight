package com.meemaw.auth.sso;

import com.meemaw.auth.sso.oauth.github.OAuth2GithubService;
import com.meemaw.auth.sso.oauth.google.OAuth2GoogleService;
import com.meemaw.auth.sso.oauth.microsoft.OAuth2MicrosoftService;
import com.meemaw.auth.sso.saml.service.SamlService;
import com.meemaw.auth.sso.setup.model.SsoMethod;
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
  @Inject SamlService samlService;

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

  private UriBuilder ssoSignInLocationBaseBuilder(SsoMethod method, URI serverBaseURI) {
    AbstractIdpService idpService = getService(method);
    return UriBuilder.fromUri(serverBaseURI).path(idpService.signInPath());
  }

  public URI ssoSignInLocationBase(SsoMethod method, URI serverBaseURI) {
    return ssoSignInLocationBaseBuilder(method, serverBaseURI).build();
  }

  public URI ssoSignInLocation(SsoMethod method, String email, URI serverBaseURI, URL redirect) {
    return ssoSignInLocationBaseBuilder(method, serverBaseURI)
        .queryParam("redirect", redirect)
        .queryParam("email", email)
        .build();
  }

  public Function<String, Response> ssoSignInRedirect(
      SsoMethod method, String email, URI serverBaseURI, URL redirect) {
    URI location = ssoSignInLocation(method, email, serverBaseURI, redirect);
    return (cookieDomain) -> Response.status(Status.FOUND).header("Location", location).build();
  }
}

package com.meemaw.auth.sso;

import com.meemaw.auth.sso.oauth.github.GithubIdentityProvider;
import com.meemaw.auth.sso.oauth.google.GoogleIdentityProvider;
import com.meemaw.auth.sso.oauth.microsoft.MicrosoftIdentityProvider;
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

  private UriBuilder ssoSignInLocationBaseBuilder(SsoMethod method, URI serverBaseURI) {
    AbstractIdentityProvider idpService = getService(method);
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

  public Function<String, Response.ResponseBuilder> ssoSignInRedirectResponse(
      SsoMethod method, String email, URI serverBaseURI, URL redirect) {
    URI location = ssoSignInLocation(method, email, serverBaseURI, redirect);
    return (cookieDomain) -> Response.status(Status.FOUND).header("Location", location);
  }
}

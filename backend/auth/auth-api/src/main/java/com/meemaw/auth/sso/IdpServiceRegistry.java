package com.meemaw.auth.sso;

import com.meemaw.auth.sso.oauth.shared.OAuth2Resource;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;

@ApplicationScoped
public class IdpServiceRegistry {

  public String signInLocation(
      String serverBaseURL, SsoMethod ssoMethod, String email, String redirect) {
    return UriBuilder.fromUri(serverBaseURL)
        .path(SsoResource.PATH)
        .path(ssoMethod.getKey())
        .path(OAuth2Resource.SIGNIN_PATH)
        .queryParam("redirect", redirect)
        .queryParam("email", email)
        .build()
        .toString();
  }
}

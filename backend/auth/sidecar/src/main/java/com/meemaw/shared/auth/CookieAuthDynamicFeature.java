package com.meemaw.shared.auth;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class CookieAuthDynamicFeature extends AbstractCookieAuthDynamicFeature {

  @Inject
  SsoSessionClient ssoSessionClient;

  @Override
  protected ContainerRequestFilter cookieAuthFilter() {
    return new CookieAuthFilter();
  }

  private class CookieAuthFilter extends AbstractCookieAuthFilter<AuthUser> {

    @Override
    protected CompletionStage<Optional<AuthUser>> findSession(String sessionId) {
      return ssoSessionClient.session(sessionId).subscribeAsCompletionStage();
    }
  }

}

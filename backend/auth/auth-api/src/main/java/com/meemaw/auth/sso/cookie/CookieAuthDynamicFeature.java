package com.meemaw.auth.sso.cookie;

import com.meemaw.auth.sso.datasource.SsoDatasource;
import com.meemaw.auth.sso.model.SsoUser;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class CookieAuthDynamicFeature extends AbstractCookieAuthDynamicFeature {

  @Inject SsoDatasource ssoDatasource;

  @Override
  protected ContainerRequestFilter cookieAuthFilter() {
    return new CookieAuthFilter();
  }

  private class CookieAuthFilter extends AbstractCookieAuthFilter<SsoUser> {

    @Override
    protected CompletionStage<Optional<SsoUser>> findSession(String sessionId) {
      return ssoDatasource.findSession(sessionId);
    }
  }
}

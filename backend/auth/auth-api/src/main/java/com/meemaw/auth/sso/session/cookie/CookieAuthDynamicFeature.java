package com.meemaw.auth.sso.session.cookie;

import com.meemaw.auth.sso.cookie.AbstractCookieAuthDynamicFeature;
import com.meemaw.auth.sso.session.datasource.SsoDatasource;
import com.meemaw.auth.sso.session.model.SsoUser;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class CookieAuthDynamicFeature extends AbstractCookieAuthDynamicFeature {

  @Inject SsoDatasource ssoDatasource;

  @Override
  public ContainerRequestFilter authFilter() {
    return new CookieAuthFilter();
  }

  private class CookieAuthFilter extends AbstractCookieAuthFilter<SsoUser> {

    @Override
    protected CompletionStage<Optional<SsoUser>> findSession(String sessionId) {
      return ssoDatasource.findSession(sessionId);
    }
  }
}

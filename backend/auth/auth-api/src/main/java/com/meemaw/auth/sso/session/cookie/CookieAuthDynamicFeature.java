package com.meemaw.auth.sso.session.cookie;

import com.meemaw.auth.sso.cookie.AbstractCookieAuthDynamicFeature;
import com.meemaw.auth.sso.session.datasource.SsoDatasource;
import com.meemaw.auth.sso.session.model.SsoUser;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

@Provider
public class CookieAuthDynamicFeature extends AbstractCookieAuthDynamicFeature {

  @Inject SsoDatasource ssoDatasource;

  @Override
  protected CompletionStage<Optional<AuthUser>> findSession(String sessionId) {
    return ssoDatasource
        .findSession(sessionId)
        .thenApply(maybeSsoUser -> maybeSsoUser.map(SsoUser::dto));
  }
}

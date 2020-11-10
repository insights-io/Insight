package com.meemaw.auth.sso.session.cookie;

import com.meemaw.auth.sso.cookie.AbstractSsoSessionCookieSecurityRequirementAuthDynamicFeature;
import com.meemaw.auth.sso.session.datasource.SsoSessionDatasource;
import com.meemaw.auth.sso.session.model.SsoUser;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

@Provider
public class SsoSessionCookieSecurityRequirementAuthDynamicFeature
    extends AbstractSsoSessionCookieSecurityRequirementAuthDynamicFeature {

  @Inject SsoSessionDatasource ssoSessionDatasource;

  @Override
  protected CompletionStage<Optional<AuthUser>> findSession(String sessionId) {
    return ssoSessionDatasource
        .retrieve(sessionId)
        .thenApply(maybeSsoUser -> maybeSsoUser.map(SsoUser::dto));
  }
}

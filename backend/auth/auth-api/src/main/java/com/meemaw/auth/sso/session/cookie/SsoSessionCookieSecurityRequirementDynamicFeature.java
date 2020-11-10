package com.meemaw.auth.sso.session.cookie;

import com.meemaw.auth.sso.cookie.AbstractSsoSessionCookieSecurityRequirementDynamicFeature;
import com.meemaw.auth.sso.session.datasource.SsoSessionDatasource;
import com.meemaw.auth.sso.session.model.SsoUser;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.opentracing.Traced;

@Provider
public class SsoSessionCookieSecurityRequirementDynamicFeature
    extends AbstractSsoSessionCookieSecurityRequirementDynamicFeature {

  @Inject SsoSessionDatasource ssoSessionDatasource;

  @Traced
  @Override
  protected CompletionStage<Optional<AuthUser>> findSession(String cookieValue) {
    return ssoSessionDatasource
        .retrieve(cookieValue)
        .thenApply(maybeSsoUser -> maybeSsoUser.map(SsoUser::dto));
  }
}

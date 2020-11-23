package com.meemaw.auth.sso.token.bearer;

import com.meemaw.auth.sso.bearer.AbstractBearerTokenSecurityRequirementAuthDynamicFeature;
import com.meemaw.auth.sso.token.datasource.AuthTokenDatasource;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.opentracing.Traced;

@Provider
public class BearerTokenSecurityRequirementDynamicFeature
    extends AbstractBearerTokenSecurityRequirementAuthDynamicFeature {

  @Inject AuthTokenDatasource authTokenDatasource;

  @Override
  @Traced
  public CompletionStage<Optional<AuthUser>> findUser(String token) {
    return authTokenDatasource.getTokenUser(token);
  }
}

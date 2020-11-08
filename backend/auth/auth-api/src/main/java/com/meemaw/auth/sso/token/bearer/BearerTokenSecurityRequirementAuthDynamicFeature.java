package com.meemaw.auth.sso.token.bearer;

import com.meemaw.auth.sso.bearer.AbstractBearerTokenSecurityRequirementAuthDynamicFeature;
import com.meemaw.auth.sso.token.datasource.AuthTokenDatasource;
import com.meemaw.auth.user.model.AuthUser;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

@Provider
public class BearerTokenSecurityRequirementAuthDynamicFeature
    extends AbstractBearerTokenSecurityRequirementAuthDynamicFeature {

  @Inject AuthTokenDatasource authTokenDatasource;

  @Override
  public CompletionStage<Optional<AuthUser>> findUser(String token) {
    return authTokenDatasource.getTokenUser(token);
  }
}

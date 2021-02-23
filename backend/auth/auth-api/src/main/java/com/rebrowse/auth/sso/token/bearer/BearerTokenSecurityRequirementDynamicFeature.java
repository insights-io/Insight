package com.rebrowse.auth.sso.token.bearer;

import com.rebrowse.auth.sso.bearer.AbstractBearerTokenSecurityRequirementAuthDynamicFeature;
import com.rebrowse.auth.sso.token.datasource.AuthTokenDatasource;
import com.rebrowse.auth.user.model.AuthUser;
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

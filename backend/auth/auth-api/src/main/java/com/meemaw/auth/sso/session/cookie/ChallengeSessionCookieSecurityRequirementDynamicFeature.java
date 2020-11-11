package com.meemaw.auth.sso.session.cookie;

import com.meemaw.auth.sso.cookie.AbstractChallengeSecurityRequirementDynamicFeature;
import com.meemaw.auth.tfa.challenge.datasource.HazelcastTfaChallengeDatasource;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.opentracing.Traced;

@Provider
public class ChallengeSessionCookieSecurityRequirementDynamicFeature
    extends AbstractChallengeSecurityRequirementDynamicFeature {

  @Inject UserDatasource userDatasource;
  @Inject HazelcastTfaChallengeDatasource challengeDatasource;

  @Traced
  @Override
  protected CompletionStage<Optional<AuthUser>> findSession(String cookieValue) {
    return challengeDatasource
        .retrieve(cookieValue)
        .thenCompose(
            maybeUserId -> {
              if (maybeUserId.isEmpty()) {
                return CompletableFuture.completedStage(Optional.empty());
              }

              // TODO: should probably use user cache
              return userDatasource.findUser(maybeUserId.get());
            });
  }
}

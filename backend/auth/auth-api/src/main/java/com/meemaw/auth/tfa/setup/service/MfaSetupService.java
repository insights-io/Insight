package com.meemaw.auth.tfa.setup.service;

import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.tfa.MfaMethod;
import com.meemaw.auth.tfa.MfaProvider;
import com.meemaw.auth.tfa.MfaProvidersRegistry;
import com.meemaw.auth.tfa.model.MfaConfiguration;
import com.meemaw.auth.user.datasource.UserMfaDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.response.Boom;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@ApplicationScoped
@Slf4j
public class MfaSetupService {

  @Inject UserMfaDatasource userMfaDatasource;
  @Inject MfaProvidersRegistry mfaProvidersRegistry;

  public CompletionStage<Boolean> mfaSetupDisable(UUID userId, MfaMethod method) {
    log.debug("[AUTH]: {} MFA setup disable for user={}", method, userId);
    return userMfaDatasource
        .delete(userId, method)
        .thenApply(
            deleted -> {
              if (!deleted) {
                throw Boom.notFound().exception();
              }
              log.debug("[AUTH]: {} MFA setup disabled for user={}", method, userId);
              return true;
            });
  }

  public CompletionStage<?> mfaSetupStart(MfaMethod method, InsightPrincipal principal) {
    MfaProvider<?> mfaProvider = mfaProvidersRegistry.get(method);
    log.debug("[AUTH]: {} MFA setup start for user={}", method, principal.user().getId());
    return mfaProvider.setupStart(principal.user(), principal.challengeId() != null);
  }

  public CompletionStage<Pair<MfaConfiguration, AuthUser>> mfaSetupComplete(
      MfaMethod method, AuthUser user, int code) {
    MfaProvider<?> mfaProvider = mfaProvidersRegistry.get(method);
    log.debug("[AUTH]: {} MFA setup complete for user={} code={}", method, user.getId(), code);
    return mfaProvider.setupComplete(user, code);
  }
}

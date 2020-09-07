package com.meemaw.auth.sso.tfa.setup.service;

import com.meemaw.auth.sso.tfa.TfaMethod;
import com.meemaw.auth.sso.tfa.TfaProvider;
import com.meemaw.auth.sso.tfa.TfaProvidersRegistry;
import com.meemaw.auth.sso.tfa.setup.model.TfaSetup;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class TfaSetupService {

  @Inject UserTfaDatasource userTfaDatasource;
  @Inject TfaProvidersRegistry tfaProvidersRegistry;

  @ConfigProperty(name = "tfa.totp.verification.issuer")
  String issuer;

  public CompletionStage<Boolean> tfaSetupDisable(UUID userId, TfaMethod method) {
    log.info("[AUTH]: {} TFA setup disable for user={}", method, userId);
    return userTfaDatasource
        .delete(userId, method)
        .thenApply(
            deleted -> {
              if (deleted) {
                log.info("[AUTH]: {} TFA setup disabled for user={}", method, userId);
              }
              return deleted;
            });
  }

  public CompletionStage<?> tfaSetupStart(TfaMethod method, UUID userId, String email) {
    TfaProvider<?> tfaProvider = tfaProvidersRegistry.get(method);
    log.info("[AUTH]: {} TFA setup start for user={}", method, userId);
    return tfaProvider.setupStart(userId, email);
  }

  public CompletionStage<TfaSetup> tfaSetupComplete(TfaMethod method, UUID userId, int code) {
    TfaProvider<?> tfaProvider = tfaProvidersRegistry.get(method);
    log.info("[AUTH]: {} TFA setup complete for user={} code={}", method, userId, code);
    return tfaProvider.setupComplete(userId, code);
  }
}

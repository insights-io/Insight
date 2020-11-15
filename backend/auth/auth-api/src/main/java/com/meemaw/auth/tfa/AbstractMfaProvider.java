package com.meemaw.auth.tfa;

import com.meemaw.auth.tfa.model.MfaConfiguration;
import com.meemaw.auth.user.datasource.UserMfaDatasource;
import com.meemaw.shared.rest.response.Boom;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMfaProvider<T> implements MfaProvider<T> {

  @Inject UserMfaDatasource userMfaDatasource;

  public CompletionStage<Void> assertCanSetupMfa(UUID userId) {
    MfaMethod method = getMethod();
    return userMfaDatasource
        .get(userId, method)
        .thenApply(
            configurations -> {
              Optional<MfaConfiguration> maybeConfiguration =
                  configurations.stream()
                      .filter(config -> config.getMethod().equals(method))
                      .findFirst();

              if (maybeConfiguration.isPresent()) {
                log.debug("[AUTH]: {} MFA setup already set up for user={}", method, userId);
                throw Boom.badRequest()
                    .message(String.format("%s MFA already set up", method))
                    .exception();
              }

              return null;
            });
  }
}

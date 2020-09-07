package com.meemaw.auth.sso.tfa;

import com.meemaw.auth.sso.tfa.setup.model.TfaSetup;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.shared.rest.response.Boom;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTfaProvider<T> implements TfaProvider<T> {

  @Inject UserTfaDatasource userTfaDatasource;

  public CompletionStage<Void> assertCanSetupTfa(UUID userId) {
    TfaMethod method = getMethod();
    return userTfaDatasource
        .get(userId, method)
        .thenApply(
            tfaSetups -> {
              Optional<TfaSetup> maybeTfaSetup =
                  tfaSetups.stream()
                      .filter(tfaSetup -> tfaSetup.getMethod().equals(method))
                      .findFirst();

              if (maybeTfaSetup.isPresent()) {
                log.debug("[AUTH]: {} TFA setup already set up for user={}", method, userId);
                throw Boom.badRequest()
                    .message(String.format("%s TFA already set up", method))
                    .exception();
              }

              return null;
            });
  }
}

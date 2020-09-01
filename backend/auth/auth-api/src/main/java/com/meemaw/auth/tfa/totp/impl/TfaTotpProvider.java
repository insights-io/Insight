package com.meemaw.auth.tfa.totp.impl;

import com.meemaw.auth.tfa.AbstractTfaProvider;
import com.meemaw.auth.tfa.TfaChallengeValidatationException;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.challenge.service.TfaChallengeService;
import com.meemaw.auth.tfa.setup.model.TfaSetup;
import com.meemaw.auth.tfa.totp.datasource.TfaTotpSetupDatasource;
import com.meemaw.auth.tfa.totp.model.TfaTotpSetup;
import com.meemaw.auth.tfa.totp.model.dto.TfaTotpSetupStartDTO;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.shared.io.IoUtils;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.client.SqlPool;
import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class TfaTotpProvider extends AbstractTfaProvider<TfaTotpSetupStartDTO> {

  @Inject TfaTotpSetupDatasource tfaTotpSetupDatasource;
  @Inject UserTfaDatasource userTfaDatasource;
  @Inject SqlPool sqlPool;

  @ConfigProperty(name = "tfa.totp.verification.issuer")
  String issuer;

  @Override
  public CompletionStage<Boolean> validate(int code, TfaSetup tfaSetup)
      throws TfaChallengeValidatationException {
    String base32secret = ((TfaTotpSetup) tfaSetup).getSecret();
    try {
      return CompletableFuture.completedStage(TotpUtils.validate(base32secret, code));
    } catch (GeneralSecurityException ex) {
      throw new TfaChallengeValidatationException(ex);
    }
  }

  @Override
  public CompletionStage<TfaTotpSetupStartDTO> setupStart(UUID userId, String email) {
    return assertCanSetupTfa(userId)
        .thenCompose(
            i1 -> {
              String secret = TotpUtils.generateSecret();
              return tfaTotpSetupDatasource
                  .setTotpSecret(userId, secret)
                  .thenApply(
                      i2 -> {
                        String keyId = String.format("%s:%s", issuer, email);
                        ByteArrayOutputStream qrImage =
                            TotpUtils.generateQrImage(keyId, secret, issuer);
                        return new TfaTotpSetupStartDTO(IoUtils.base64encodeImage(qrImage));
                      });
            });
  }

  @Override
  public TfaMethod getMethod() {
    return TfaMethod.TOTP;
  }

  @Override
  public CompletionStage<TfaSetup> setupComplete(UUID userId, int code) {
    return tfaTotpSetupDatasource
        .getTotpSecret(userId)
        .thenCompose(
            maybeSecret -> {
              if (maybeSecret.isEmpty()) {
                log.debug("[AUTH]: TFA TOTP setup complete session expired for user={}", userId);
                throw Boom.badRequest().message("Code expired").exception();
              }

              try {
                boolean isValid = TotpUtils.validate(maybeSecret.get(), code);
                if (!isValid) {
                  log.debug("[AUTH]: TFA TOTP setup complete invalid code for user={}", userId);
                  throw Boom.badRequest()
                      .errors(TfaChallengeService.INVALID_CODE_ERRORS)
                      .exception();
                }

                return sqlPool
                    .beginTransaction()
                    .thenCompose(
                        transaction ->
                            userTfaDatasource
                                .storeTotpTfa(userId, maybeSecret.get(), transaction)
                                .thenCompose(
                                    tfaSetup -> {
                                      tfaTotpSetupDatasource.deleteTotpSecret(userId);
                                      return transaction.commit().thenApply(k -> tfaSetup);
                                    }))
                    .thenApply(
                        tfaSetup -> {
                          log.info(
                              "[AUTH]: TFA TOTP setup complete successful for user={}", userId);
                          return tfaSetup;
                        });
              } catch (GeneralSecurityException ex) {
                log.error(
                    "[AUTH]: Something went wrong while trying to complete TFA setup for user={}",
                    userId,
                    ex);
                throw Boom.serverError().message(ex.getMessage()).exception(ex);
              }
            });
  }
}

package com.meemaw.auth.tfa.totp.impl;

import com.meemaw.auth.tfa.AbstractMfaProvider;
import com.meemaw.auth.tfa.MfaChallengeValidatationException;
import com.meemaw.auth.tfa.MfaMethod;
import com.meemaw.auth.tfa.challenge.service.MfaChallengeService;
import com.meemaw.auth.tfa.model.MfaConfiguration;
import com.meemaw.auth.tfa.totp.datasource.MfaTotpSetupDatasource;
import com.meemaw.auth.tfa.totp.model.MfaConfigurationDTO;
import com.meemaw.auth.tfa.totp.model.dto.MfaTotpSetupStartDTO;
import com.meemaw.auth.user.datasource.UserMfaDatasource;
import com.meemaw.auth.user.model.AuthUser;
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
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class MfaTotpProvider extends AbstractMfaProvider<MfaTotpSetupStartDTO> {

  @Inject MfaTotpSetupDatasource mfaTotpSetupDatasource;
  @Inject UserMfaDatasource userMfaDatasource;
  @Inject SqlPool sqlPool;

  @ConfigProperty(name = "authorization.issuer")
  String issuer;

  @Override
  public CompletionStage<Boolean> completeChallenge(
      String challengeId, int code, MfaConfiguration mfaConfiguration)
      throws MfaChallengeValidatationException {
    String base32secret = ((MfaConfigurationDTO) mfaConfiguration).getSecret();
    try {
      return CompletableFuture.completedStage(TotpUtils.validate(base32secret, code));
    } catch (GeneralSecurityException ex) {
      throw new MfaChallengeValidatationException(ex);
    }
  }

  @Override
  public CompletionStage<MfaTotpSetupStartDTO> setupStart(AuthUser user, boolean isChallenged) {
    UUID userId = user.getId();
    return assertCanSetupMfa(userId)
        .thenCompose(
            i1 -> {
              String secret = TotpUtils.generateSecret();
              return mfaTotpSetupDatasource
                  .set(userId, secret)
                  .thenApply(
                      i2 -> {
                        String qrImageKeyId = String.format("%s:%s", issuer, user.getEmail());
                        ByteArrayOutputStream qrImage =
                            TotpUtils.generateQrImage(qrImageKeyId, secret, issuer);
                        return new MfaTotpSetupStartDTO(IoUtils.base64encodeImage(qrImage));
                      });
            });
  }

  @Override
  public MfaMethod getMethod() {
    return MfaMethod.TOTP;
  }

  @Override
  public CompletionStage<Pair<MfaConfiguration, AuthUser>> setupComplete(AuthUser user, int code) {
    UUID userId = user.getId();
    return mfaTotpSetupDatasource
        .retrieve(userId)
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
                      .errors(MfaChallengeService.INVALID_CODE_ERRORS)
                      .exception();
                }

                return sqlPool
                    .beginTransaction()
                    .thenCompose(
                        transaction ->
                            userMfaDatasource
                                .storeTotpTfa(userId, maybeSecret.get(), transaction)
                                .thenCompose(
                                    tfaSetup -> {
                                      mfaTotpSetupDatasource.delete(userId);
                                      return transaction.commit().thenApply(k -> tfaSetup);
                                    }))
                    .thenApply(tfaSetup -> Pair.of(tfaSetup, user));
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

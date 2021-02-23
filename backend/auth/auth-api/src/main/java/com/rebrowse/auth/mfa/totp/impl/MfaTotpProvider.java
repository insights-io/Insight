package com.rebrowse.auth.mfa.totp.impl;

import com.rebrowse.auth.mfa.AbstractMfaProvider;
import com.rebrowse.auth.mfa.MfaChallengeValidatationException;
import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.model.MfaConfiguration;
import com.rebrowse.auth.mfa.setup.service.MfaAuthorizationChallengeService;
import com.rebrowse.auth.mfa.totp.datasource.MfaTotpSetupDatasource;
import com.rebrowse.auth.mfa.totp.model.MfaConfigurationDTO;
import com.rebrowse.auth.mfa.totp.model.dto.MfaTotpSetupStartDTO;
import com.rebrowse.auth.user.datasource.UserMfaDatasource;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.io.IoUtils;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.sql.client.SqlPool;
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

  @Inject
  MfaTotpSetupDatasource mfaTotpSetupDatasource;
  @Inject
  UserMfaDatasource userMfaDatasource;
  @Inject SqlPool sqlPool;

  @ConfigProperty(name = "authorization.issuer")
  String issuer;

  @Override
  public MfaMethod getMethod() {
    return MfaMethod.TOTP;
  }

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
  public CompletionStage<MfaTotpSetupStartDTO> startSetup(String identifier, AuthUser user) {
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
  public CompletionStage<Pair<MfaConfiguration, AuthUser>> completeSetup(
      String identifier, AuthUser user, int code) {
    UUID userId = user.getId();

    return mfaTotpSetupDatasource
        .retrieve(userId)
        .thenCompose(
            maybeTotpSecret -> {
              String totpSecret =
                  maybeTotpSecret.orElseThrow(
                      () -> Boom.notFound().message("Code expired").exception());

              try {
                boolean isValid = TotpUtils.validate(totpSecret, code);
                if (!isValid) {
                  log.debug("[AUTH]: MFA TOTP setup complete invalid code for user={}", userId);
                  throw Boom.badRequest()
                      .errors(MfaAuthorizationChallengeService.INVALID_CODE_ERRORS)
                      .exception();
                }

                return sqlPool
                    .beginTransaction()
                    .thenCompose(
                        transaction ->
                            userMfaDatasource
                                .createTotpConfiguration(userId, totpSecret, transaction)
                                .thenCompose(
                                    configuration -> {
                                      mfaTotpSetupDatasource.delete(userId);
                                      return transaction
                                          .commit()
                                          .thenApply(k -> Pair.of(configuration, user));
                                    }));
              } catch (GeneralSecurityException ex) {
                log.error(
                    "[AUTH]: Something went wrong while trying to complete MFA setup for user={}",
                    userId,
                    ex);
                throw Boom.serverError().message(ex.getMessage()).exception(ex);
              }
            });
  }
}

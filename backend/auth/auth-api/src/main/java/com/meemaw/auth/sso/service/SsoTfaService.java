package com.meemaw.auth.sso.service;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import com.meemaw.auth.sso.datasource.SsoVerificationDatasource;
import com.meemaw.auth.sso.model.dto.TfaSetupStartDTO;
import com.meemaw.auth.sso.service.exception.VerificationSessionExpiredException;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.user.model.TfaSetup;
import com.meemaw.shared.io.IoUtils;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class SsoTfaService {

  private static final Map<String, String> INVALID_CODE_ERRORS = Map.of("code", "Invalid code");

  @ConfigProperty(name = "sso.verification.issuer")
  String issuer;

  @Inject UserTfaDatasource userTfaDatasource;
  @Inject SsoVerificationDatasource ssoVerificationDatasource;
  @Inject SqlPool sqlPool;
  @Inject SsoService ssoService;

  public CompletionStage<String> tfaComplete(int code, String verificationId) {
    return ssoVerificationDatasource
        .retrieveUserByVerificationId(verificationId)
        .thenCompose(
            maybeUser -> {
              UUID userId =
                  maybeUser.orElseThrow(
                      () -> {
                        log.info("[AUTH]: Verification session {} expired", verificationId);
                        throw new VerificationSessionExpiredException();
                      });

              return userTfaDatasource
                  .get(userId)
                  .thenCompose(
                      maybeTfaSetup -> {
                        TfaSetup tfaSetup =
                            maybeTfaSetup.orElseThrow(
                                () -> {
                                  log.info(
                                      "[AUTH]: TFA complete attempt for user={} with no TFA configured",
                                      userId);
                                  throw Boom.badRequest().message("TFA not configured").exception();
                                });

                        try {
                          String base32secret = tfaSetup.getSecret();
                          boolean isValid =
                              TimeBasedOneTimePasswordUtil.validateCurrentNumber(
                                  base32secret, code, 0);
                          if (!isValid) {
                            log.info(
                                "[AUTH]: Invalid code for TFA verification for user={}", userId);
                            throw Boom.badRequest().errors(INVALID_CODE_ERRORS).exception();
                          }

                          return ssoVerificationDatasource
                              .deleteVerificationId(verificationId)
                              .thenCompose(ignored -> ssoService.createSession(userId));
                        } catch (GeneralSecurityException ex) {
                          log.error(
                              "[AUTH]: Something went wrong while validating TFA code for user={}",
                              userId,
                              ex);
                          throw Boom.serverError().exception(ex);
                        }
                      });
            });
  }

  public CompletionStage<Boolean> tfaSetupDisable(UUID userId) {
    log.info("[AUTH]: TFA setup disable request for user={}", userId);
    return userTfaDatasource
        .delete(userId)
        .thenApply(
            deleted -> {
              if (deleted) {
                log.info("[AUTH]: TFA setup disabled for user={}", userId);
              }
              return deleted;
            });
  }

  public CompletionStage<TfaSetupStartDTO> tfaSetupStart(UUID userId, String email) {
    log.info("[AUTH]: TFA setup request for user={}", userId);
    return userTfaDatasource
        .get(userId)
        .thenCompose(
            maybeTfaSetup -> {
              if (maybeTfaSetup.isPresent()) {
                log.debug("[AUTH]: TFA setup already set up for user={}", userId);
                throw Boom.badRequest().message("TFA already set up").exception();
              }

              String secret = TimeBasedOneTimePasswordUtil.generateBase32Secret();
              return ssoVerificationDatasource
                  .setTfaSetupSecret(userId, secret)
                  .thenApply(
                      nothing -> {
                        String keyId = String.format("%s:%s", issuer, email);
                        String imageURL = generateQrImageUrl(keyId, secret, issuer);
                        try {
                          return new TfaSetupStartDTO(IoUtils.base64encodeImage(imageURL));
                        } catch (IOException ex) {
                          log.error(
                              "[AUTH]: Failed to Base64 encode TFA QR image={}", imageURL, ex);
                          throw Boom.serverError().exception(ex);
                        }
                      });
            });
  }

  public CompletionStage<TfaSetup> tfaSetupComplete(UUID userId, int code) {
    log.info("[AUTH]: TFA setup complete request for user={}", userId);
    return ssoVerificationDatasource
        .getTfaSetupSecret(userId)
        .thenCompose(
            maybeSecret -> {
              if (maybeSecret.isEmpty()) {
                log.debug("[AUTH]: TFA setup complete QR code expired for user={}", userId);
                throw Boom.badRequest().message("QR code expired").exception();
              }

              try {
                boolean isValid =
                    TimeBasedOneTimePasswordUtil.validateCurrentNumber(maybeSecret.get(), code, 0);

                if (!isValid) {
                  log.debug("[AUTH]: TFA setup complete invalid code for user={}", userId);
                  throw Boom.badRequest().errors(INVALID_CODE_ERRORS).exception();
                }

                return sqlPool
                    .beginTransaction()
                    .thenCompose(transaction -> tfaComplete(userId, maybeSecret.get(), transaction))
                    .thenApply(
                        tfaSetup -> {
                          log.info("[AUTH]: TFA setup complete successful for user={}", userId);
                          return tfaSetup;
                        });
              } catch (GeneralSecurityException ex) {
                log.error(
                    "[AUTH]: Something went wrong while trying to complete TFA setup for user={}",
                    userId,
                    ex);
                throw Boom.serverError().message(ex.getMessage()).exception();
              }
            });
  }

  private CompletionStage<TfaSetup> tfaComplete(
      UUID userId, String secret, SqlTransaction transaction) {
    return userTfaDatasource
        .store(userId, secret, transaction)
        .thenCompose(
            tfaSetup ->
                ssoVerificationDatasource
                    .deleteTfaSetupSecret(userId)
                    .thenCompose(ignored1 -> transaction.commit().thenApply(ignored2 -> tfaSetup)));
  }

  private String generateQrImageUrl(String keyId, String secret, String issuer) {
    return new StringBuilder(117 + keyId.length() + secret.length() + issuer.length())
        .append("https://chart.googleapis.com/chart")
        .append("?chs=200x200&cht=qr&chl=200x200&chld=M|0&cht=qr&chl=")
        .append("otpauth://totp/")
        .append(keyId)
        .append("?secret=")
        .append(secret)
        .append("&issuer=")
        .append(issuer)
        .toString();
  }
}

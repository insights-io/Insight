package com.meemaw.auth.verification.service;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.user.model.TfaSetup;
import com.meemaw.auth.verification.datasource.TfaSetupDatasource;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class TfaService {

  private static final String ISSUER = "Insight";

  @Inject UserTfaDatasource userTfaDatasource;
  @Inject TfaSetupDatasource tfaSetupDatasource;
  @Inject SqlPool sqlPool;

  public CompletionStage<String> tfaSetup(UUID userId, String email) {
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
              return tfaSetupDatasource
                  .storeTfaSetupSecret(userId, secret)
                  .thenApply(
                      nothing -> {
                        String keyId = String.format("%s:%s", ISSUER, email);
                        return generateQrImageUrl(keyId, secret, ISSUER);
                      });
            });
  }

  public CompletionStage<TfaSetup> tfaComplete(UUID userId, int code) {
    log.info("[AUTH]: TFA setup complete request for user={}", userId);
    return tfaSetupDatasource
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
                  throw Boom.badRequest().message("Invalid code").exception();
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
                tfaSetupDatasource
                    .removeTfaSetupSecret(userId)
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

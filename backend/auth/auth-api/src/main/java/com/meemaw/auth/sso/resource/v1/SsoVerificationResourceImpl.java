package com.meemaw.auth.sso.resource.v1;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;
import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.auth.sso.model.TFASetupCompleteDTO;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.verification.datasource.VerificationDatasource;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsoVerificationResourceImpl implements SsoVerificationResource {

  private static final String ISSUER = "Insight";

  @Inject InsightPrincipal principal;
  @Inject VerificationDatasource verificationDatasource;

  @Override
  public CompletionStage<Response> tfaSetupStart() {
    AuthUser user = principal.user();
    String secret = TimeBasedOneTimePasswordUtil.generateBase32Secret();

    return verificationDatasource
        .storeTfaSetupSecret(user.getId(), secret)
        .thenApply(
            nothing -> {
              String keyId = String.format("%s:%s", ISSUER, user.getEmail());
              String qrImageUrl = generateQrImageUrl(keyId, secret, ISSUER);
              return DataResponse.ok(Map.of("qrImageUrl", qrImageUrl));
            });
  }

  @Override
  public CompletionStage<Response> tfaSetupComplete(TFASetupCompleteDTO body) {
    AuthUser user = principal.user();
    return verificationDatasource
        .getTfaSetupSecret(user.getId())
        .thenApply(
            maybeSecret -> {
              if (maybeSecret.isEmpty()) {
                return Boom.badRequest().message("QR code expired").response();
              }

              try {
                boolean isValid =
                    TimeBasedOneTimePasswordUtil.validateCurrentNumber(
                        maybeSecret.get(), body.getCode(), 0);

                if (!isValid) {
                  return Boom.badRequest().message("Invalid code").response();
                }

                return DataResponse.ok(true);
              } catch (GeneralSecurityException ex) {
                log.error("[AUTH]: Something went wrong while trying to setup TFA", ex);
                return Boom.serverError().message(ex.getMessage()).response();
              }
            });
  }

  private String generateQrImageUrl(String keyId, String secret, String issuer) {
    return new StringBuilder(128)
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

package com.rebrowse.auth.mfa.sms.impl;

import com.rebrowse.auth.mfa.AbstractMfaProvider;
import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.mfa.dto.MfaChallengeCodeDetailsDTO;
import com.rebrowse.auth.mfa.model.MfaConfiguration;
import com.rebrowse.auth.mfa.setup.service.MfaAuthorizationChallengeService;
import com.rebrowse.auth.sso.session.model.AuthPrincipal;
import com.rebrowse.auth.user.datasource.UserMfaDatasource;
import com.rebrowse.auth.user.datasource.UserTable;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.PhoneNumber;
import com.rebrowse.auth.user.phone.datasource.UserPhoneCodeDatasource;
import com.rebrowse.auth.user.phone.service.UserPhoneCodeService;
import com.rebrowse.auth.user.service.UserService;
import com.rebrowse.shared.rest.query.UpdateDTO;
import com.rebrowse.shared.rest.response.Boom;
import com.rebrowse.shared.sql.client.SqlPool;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class MfaSmsProvider extends AbstractMfaProvider<MfaChallengeCodeDetailsDTO> {

  @Inject UserPhoneCodeDatasource userPhoneCodeDatasource;
  @Inject UserMfaDatasource userMfaDatasource;
  @Inject SqlPool sqlPool;
  @Inject UserPhoneCodeService userPhoneCodeService;
  @Inject UserService userService;

  // TODO: should be 3 separate caches
  public static String verifyCodeKey(AuthPrincipal principal) {
    return verifyCodeKey(principal.getIdentifier(), principal.user().getId());
  }

  public static String verifyCodeKey(String authIdentifier, UUID userId) {
    return String.format("%s-%s-verify", authIdentifier, userId);
  }

  public static String setupCodeKey(String identifier, UUID userId) {
    return String.format("%s-%s-setup", identifier, userId);
  }

  public static String challengeCodeKey(String challengeId) {
    return String.format("%s-challenge", challengeId);
  }

  @Override
  public MfaMethod getMethod() {
    return MfaMethod.SMS;
  }

  @Override
  public CompletionStage<Boolean> completeChallenge(
      String challengeId, int code, MfaConfiguration mfaConfiguration) {
    String codeKey = challengeCodeKey(challengeId);
    return userPhoneCodeService
        .validate(code, codeKey)
        .thenApply(
            isValid -> {
              if (isValid) {
                userPhoneCodeDatasource.deleteCode(codeKey);
              }
              return isValid;
            });
  }

  @Override
  @Traced
  public CompletionStage<Pair<MfaConfiguration, AuthUser>> completeSetup(
      String sessionId, AuthUser user, int actualCode) {
    UUID userId = user.getId();
    String codeKey = setupCodeKey(sessionId, userId);
    return userPhoneCodeService
        .validate(actualCode, codeKey)
        .thenCompose(
            isValid -> {
              if (!isValid) {
                log.debug("[AUTH]: MFA SMS setup complete code invalid for user={}", userId);
                throw Boom.badRequest()
                    .errors(MfaAuthorizationChallengeService.INVALID_CODE_ERRORS)
                    .exception();
              }

              return sqlPool
                  .beginTransaction()
                  .thenCompose(
                      transaction -> {
                        final CompletableFuture<AuthUser> userUpdate =
                            user.isPhoneNumberVerified()
                                ? CompletableFuture.completedFuture(user)
                                : userService
                                    .updateUser(
                                        user.getId(),
                                        UpdateDTO.from(
                                            Map.of(UserTable.PHONE_NUMBER_VERIFIED, true)),
                                        transaction)
                                    .toCompletableFuture();

                        CompletableFuture<MfaConfiguration> storeMfaConfiguration =
                            userMfaDatasource
                                .createSmsConfiguration(userId, transaction)
                                .toCompletableFuture();

                        return CompletableFuture.allOf(userUpdate, storeMfaConfiguration)
                            .thenCompose(
                                i1 ->
                                    transaction
                                        .commit()
                                        .thenApply(
                                            i2 -> {
                                              userPhoneCodeDatasource.deleteCode(codeKey);
                                              return Pair.of(
                                                  storeMfaConfiguration.join(), userUpdate.join());
                                            }));
                      });
            });
  }

  @Override
  public CompletionStage<MfaChallengeCodeDetailsDTO> startSetup(String identifier, AuthUser user) {
    if (user.getPhoneNumber() == null) {
      throw Boom.badRequest().errors(UserTable.Errors.PHONE_NUMBER_REQUIRED).exception();
    }

    UUID userId = user.getId();
    String codeKey = setupCodeKey(identifier, userId);

    return assertCanSetupMfa(userId)
        .thenCompose(i1 -> sendVerificationCode(codeKey, user.getPhoneNumber()));
  }

  public CompletionStage<MfaChallengeCodeDetailsDTO> sendVerificationCode(
      String key, PhoneNumber phoneNumber) {
    return userPhoneCodeService
        .sendVerificationCode(key, phoneNumber)
        .thenApply(MfaChallengeCodeDetailsDTO::new);
  }
}

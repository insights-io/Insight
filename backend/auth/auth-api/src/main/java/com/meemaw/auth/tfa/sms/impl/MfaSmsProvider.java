package com.meemaw.auth.tfa.sms.impl;

import com.meemaw.auth.tfa.AbstractMfaProvider;
import com.meemaw.auth.tfa.MfaMethod;
import com.meemaw.auth.tfa.challenge.service.MfaChallengeService;
import com.meemaw.auth.tfa.dto.MfaChallengeCodeDetailsDTO;
import com.meemaw.auth.tfa.model.MfaConfiguration;
import com.meemaw.auth.user.datasource.UserMfaDatasource;
import com.meemaw.auth.user.datasource.UserTable;
import com.meemaw.auth.user.datasource.UserTable.Errors;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.auth.user.phone.datasource.UserPhoneCodeDatasource;
import com.meemaw.auth.user.phone.service.UserPhoneCodeService;
import com.meemaw.auth.user.service.UserService;
import com.meemaw.shared.rest.query.UpdateDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.sql.client.SqlPool;
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
  // TODO: should be tied to sessionId
  public static String verifyCodeKey(UUID userId) {
    return String.format("%s-verify", userId);
  }

  // TODO: should be tied to sessionId
  public static String setupCodeKey(UUID userId) {
    return String.format("%s-setup", userId);
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
  public CompletionStage<Pair<MfaConfiguration, AuthUser>> setupComplete(
      AuthUser user, int actualCode) {
    UUID userId = user.getId();
    String codeKey = setupCodeKey(userId);
    return userPhoneCodeService
        .validate(actualCode, codeKey)
        .thenCompose(
            isValid -> {
              if (!isValid) {
                log.debug("[AUTH]: MFA SMS setup complete code invalid for user={}", userId);
                throw Boom.badRequest().errors(MfaChallengeService.INVALID_CODE_ERRORS).exception();
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
                                .storeSmsTfa(userId, transaction)
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
  @Traced
  public CompletionStage<MfaChallengeCodeDetailsDTO> setupStart(
      AuthUser user, boolean isChallenged) {

    if (user.getPhoneNumber() == null) {
      throw Boom.badRequest().errors(Errors.PHONE_NUMBER_REQUIRED).exception();
    }

    if (!isChallenged && !user.isPhoneNumberVerified()) {
      throw Boom.badRequest().errors(Errors.PHONE_NUMBER_VERIFICATION_REQUIRED).exception();
    }

    UUID userId = user.getId();
    return assertCanSetupMfa(userId)
        .thenCompose(i1 -> sendVerificationCode(setupCodeKey(userId), user.getPhoneNumber()));
  }

  public CompletionStage<MfaChallengeCodeDetailsDTO> sendVerificationCode(
      String key, PhoneNumber phoneNumber) {
    return userPhoneCodeService
        .sendVerificationCode(key, phoneNumber)
        .thenApply(MfaChallengeCodeDetailsDTO::new);
  }
}

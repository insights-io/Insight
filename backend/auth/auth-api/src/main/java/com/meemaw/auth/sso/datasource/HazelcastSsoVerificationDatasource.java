package com.meemaw.auth.sso.datasource;

import com.hazelcast.map.IMap;
import com.meemaw.auth.sso.model.SsoVerification;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class HazelcastSsoVerificationDatasource implements SsoVerificationDatasource {

  private IMap<UUID, String> userTfaSetupSecretMap;
  private IMap<String, UUID> verificationToUserMap;

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.user-tfa-setup-secret-map")
  String userTfaSetupSecretMapName;

  @ConfigProperty(name = "hazelcast.auth.verification-to-user-map")
  String verificationToUserMapName;

  @PostConstruct
  public void init() {
    log.info(
        "Initializing HazelcastVerificationDatasource userTfaSetupSecretMap: {} verificationToUserMap: {}",
        userTfaSetupSecretMapName,
        verificationToUserMapName);

    userTfaSetupSecretMap = hazelcastProvider.getInstance().getMap(userTfaSetupSecretMapName);
    verificationToUserMap = hazelcastProvider.getInstance().getMap(verificationToUserMapName);
  }

  @Override
  public CompletionStage<Optional<String>> getTfaSetupSecret(UUID userId) {
    return userTfaSetupSecretMap.getAsync(userId).thenApply(Optional::ofNullable);
  }

  @Override
  public CompletionStage<Void> setTfaSetupSecret(UUID userId, String secret) {
    return userTfaSetupSecretMap.setAsync(userId, secret, 10, TimeUnit.MINUTES);
  }

  @Override
  public CompletionStage<String> deleteTfaSetupSecret(UUID userId) {
    return userTfaSetupSecretMap.removeAsync(userId);
  }

  @Override
  public CompletionStage<String> createVerificationId(UUID userId) {
    String verificationId = SsoVerification.newIdentifier();
    return verificationToUserMap
        .setAsync(verificationId, userId)
        .thenApply(oldValue -> verificationId);
  }

  @Override
  public CompletionStage<Optional<UUID>> retrieveUserByVerificationId(String verificationId) {
    return verificationToUserMap.getAsync(verificationId).thenApply(Optional::ofNullable);
  }

  @Override
  public CompletionStage<Optional<UUID>> deleteVerificationId(String verificationId) {
    return verificationToUserMap.removeAsync(verificationId).thenApply(Optional::ofNullable);
  }
}

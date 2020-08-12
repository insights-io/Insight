package com.meemaw.auth.verification.datasource;

import com.hazelcast.map.IMap;
import com.meemaw.auth.sso.datasource.HazelcastProvider;
import com.meemaw.auth.sso.model.TfaClientId;
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
public class HazelcastTfaSetupDatasource implements TfaSetupDatasource {

  private IMap<UUID, String> userTfaSetupSecretMap;
  private IMap<String, UUID> tfaClientIdToUserIdMap;

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.user-tfa-setup-secret-map")
  String userTfaSetupSecretMapName;

  @ConfigProperty(name = "hazelcast.auth.tfa-client-id-to-user-id-map")
  String tfaClientIdToUserIdMapName;

  @PostConstruct
  public void init() {
    log.info(
        "Initializing HazelcastVerificationDatasource userTfaSetupSecretMap: {} tfaClientIdToUserIdMap: {}",
        userTfaSetupSecretMapName,
        tfaClientIdToUserIdMapName);

    userTfaSetupSecretMap = hazelcastProvider.getInstance().getMap(userTfaSetupSecretMapName);
    tfaClientIdToUserIdMap = hazelcastProvider.getInstance().getMap(tfaClientIdToUserIdMapName);
  }

  @Override
  public CompletionStage<Optional<String>> getTfaSetupSecret(UUID userId) {
    return userTfaSetupSecretMap.getAsync(userId).thenApply(Optional::ofNullable);
  }

  @Override
  public CompletionStage<Void> storeTfaSetupSecret(UUID userId, String secret) {
    return userTfaSetupSecretMap.setAsync(userId, secret, 10, TimeUnit.MINUTES);
  }

  @Override
  public CompletionStage<String> removeTfaSetupSecret(UUID userId) {
    return userTfaSetupSecretMap.removeAsync(userId);
  }

  @Override
  public CompletionStage<String> createTfaClientId(UUID userId) {
    String tfaClientId = TfaClientId.newIdentifier();
    return tfaClientIdToUserIdMap.setAsync(tfaClientId, userId).thenApply(oldValue -> tfaClientId);
  }

  @Override
  public CompletionStage<Optional<UUID>> retrieveUserIdFromTfaClientId(String tfaClientId) {
    return tfaClientIdToUserIdMap.getAsync(tfaClientId).thenApply(Optional::ofNullable);
  }
}

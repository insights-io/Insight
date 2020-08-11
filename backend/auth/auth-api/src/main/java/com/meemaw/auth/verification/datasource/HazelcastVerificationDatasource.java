package com.meemaw.auth.verification.datasource;

import com.hazelcast.map.IMap;
import com.meemaw.auth.sso.datasource.HazelcastProvider;
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
public class HazelcastVerificationDatasource implements VerificationDatasource {

  private IMap<UUID, String> userTfaSetupSecretMap;

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.user-tfa-setup-secret-map")
  String userTfaSetupSecretMapName;

  @PostConstruct
  public void init() {
    log.info(
        "Initializing HazelcastVerificationDatasource userTfaSetupSecretMap: {}",
        userTfaSetupSecretMapName);

    userTfaSetupSecretMap = hazelcastProvider.getInstance().getMap(userTfaSetupSecretMapName);
  }

  @Override
  public CompletionStage<Optional<String>> getTfaSetupSecret(UUID userId) {
    return userTfaSetupSecretMap.getAsync(userId).thenApply(Optional::ofNullable);
  }

  @Override
  public CompletionStage<Void> storeTfaSetupSecret(UUID userId, String secret) {
    return userTfaSetupSecretMap.setAsync(userId, secret, 10, TimeUnit.MINUTES);
  }
}

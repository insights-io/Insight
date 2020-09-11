package com.meemaw.auth.sso.tfa.totp.datasource;

import com.hazelcast.map.IMap;
import com.meemaw.auth.sso.session.datasource.hazelcast.HazelcastProvider;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class HazelcastTfaTotpSetupDatasource implements TfaTotpSetupDatasource {

  private IMap<UUID, String> tfaTotpChallengeSetupMap;

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.tfa-totp-challenge-setup-map")
  String tfaTotpChallengeSetupMapName;

  @PostConstruct
  public void init() {
    log.info("tfaTotpChallengeSetupMapName: {}", tfaTotpChallengeSetupMapName);
    tfaTotpChallengeSetupMap = hazelcastProvider.getInstance().getMap(tfaTotpChallengeSetupMapName);
  }

  @Override
  public CompletionStage<Optional<String>> getTotpSecret(UUID userId) {
    return tfaTotpChallengeSetupMap.getAsync(userId).thenApply(Optional::ofNullable);
  }

  @Override
  public CompletionStage<Void> setTotpSecret(UUID userId, String secret) {
    return tfaTotpChallengeSetupMap.setAsync(userId, secret);
  }

  @Override
  public void deleteTotpSecret(UUID userId) {
    tfaTotpChallengeSetupMap.delete(userId);
  }
}

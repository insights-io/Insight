package com.meemaw.auth.tfa.totp.datasource;

import com.hazelcast.map.IMap;
import com.meemaw.auth.tfa.model.SsoChallenge;
import com.meemaw.shared.hazelcast.cdi.HazelcastProvider;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class HazelcastTfaTotpSetupDatasource implements TfaTotpSetupDatasource {

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.tfa-totp-challenge-setup-map")
  String tfaTotpChallengeSetupMapName;

  private IMap<UUID, String> tfaTotpChallengeSetupMap;

  @PostConstruct
  public void init() {
    tfaTotpChallengeSetupMap = hazelcastProvider.getInstance().getMap(tfaTotpChallengeSetupMapName);
  }

  @Override
  public CompletionStage<Optional<String>> retrieve(UUID userId) {
    return tfaTotpChallengeSetupMap.getAsync(userId).thenApply(Optional::ofNullable);
  }

  @Override
  public CompletionStage<Void> set(UUID userId, String secret) {
    return tfaTotpChallengeSetupMap.setAsync(userId, secret, SsoChallenge.TTL, TimeUnit.SECONDS);
  }

  @Override
  public void delete(UUID userId) {
    tfaTotpChallengeSetupMap.delete(userId);
  }
}

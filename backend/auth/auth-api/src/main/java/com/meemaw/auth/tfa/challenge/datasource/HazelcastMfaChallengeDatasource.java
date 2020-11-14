package com.meemaw.auth.tfa.challenge.datasource;

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
public class HazelcastMfaChallengeDatasource implements MfaChallengeDatasource {

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.mfa-challenge-map")
  String mapName;

  private IMap<String, UUID> map;

  @PostConstruct
  public void init() {
    map = hazelcastProvider.getInstance().getMap(mapName);
  }

  @Override
  public CompletionStage<String> create(UUID userId) {
    String challengeId = SsoChallenge.newIdentifier();
    return map.setAsync(challengeId, userId, SsoChallenge.TTL, TimeUnit.SECONDS)
        .thenApply(oldValue -> challengeId);
  }

  @Override
  public CompletionStage<Optional<UUID>> retrieve(String challengeId) {
    return map.getAsync(challengeId).thenApply(Optional::ofNullable);
  }

  @Override
  public void delete(String challengeId) {
    map.delete(challengeId);
  }
}

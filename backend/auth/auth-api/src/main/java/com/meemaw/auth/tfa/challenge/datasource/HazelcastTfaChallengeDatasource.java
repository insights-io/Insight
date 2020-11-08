package com.meemaw.auth.tfa.challenge.datasource;

import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.meemaw.auth.tfa.model.SsoChallenge;
import com.meemaw.shared.hazelcast.cdi.HazelcastProvider;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Slf4j
public class HazelcastTfaChallengeDatasource implements TfaChallengeDatasource {

  private IMap<String, UUID> tfaChallengeMap;

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.tfa-challenge-map")
  String tfaChallengeMapName;

  @PostConstruct
  public void init() {
    log.info("tfaChallengeMapName: {}", tfaChallengeMapName);
    tfaChallengeMap = hazelcastProvider.getInstance().getMap(tfaChallengeMapName);
  }

  @Override
  public CompletionStage<String> createChallengeForUser(UUID userId) {
    String challengeId = SsoChallenge.newIdentifier();
    return tfaChallengeMap.setAsync(challengeId, userId).thenApply(oldValue -> challengeId);
  }

  @Override
  public CompletionStage<Optional<UUID>> retrieveUserByChallengeId(String challengeId) {
    return tfaChallengeMap.getAsync(challengeId).thenApply(Optional::ofNullable);
  }

  @Override
  public void deleteChallenge(String challengeId) {
    tfaChallengeMap.delete(challengeId);
  }
}

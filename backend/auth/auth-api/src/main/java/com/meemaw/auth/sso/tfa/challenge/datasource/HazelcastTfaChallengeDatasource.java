package com.meemaw.auth.sso.tfa.challenge.datasource;

import com.hazelcast.map.IMap;
import com.meemaw.auth.sso.session.datasource.HazelcastProvider;
import com.meemaw.auth.sso.tfa.challenge.model.SsoChallenge;
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
  public CompletionStage<String> createChallenge(UUID userId) {
    String challengeId = SsoChallenge.newIdentifier();
    return tfaChallengeMap.setAsync(challengeId, userId).thenApply(oldValue -> challengeId);
  }

  @Override
  public CompletionStage<Optional<UUID>> retrieveUser(String challengeId) {
    return tfaChallengeMap.getAsync(challengeId).thenApply(Optional::ofNullable);
  }

  @Override
  public void deleteVerification(String challengeId) {
    tfaChallengeMap.delete(challengeId);
  }
}

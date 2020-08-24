package com.meemaw.auth.tfa.sms.datasource;

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
public class HazelcastTfaSmsDatasource implements TfaSmsDatasource {

  private static final int VALIDITY_SECONDS = 60;

  private IMap<UUID, Integer> tfaSmsChallengeMap;

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.tfa-sms-challenge-map")
  String tfaSmsChallengeMapName;

  @PostConstruct
  public void init() {
    log.info("tfaSmsChallengeMap: {}", tfaSmsChallengeMapName);
    tfaSmsChallengeMap = hazelcastProvider.getInstance().getMap(tfaSmsChallengeMapName);
  }

  @Override
  public CompletionStage<Integer> setCode(UUID userId, int code) {
    return tfaSmsChallengeMap
        .setAsync(userId, code, VALIDITY_SECONDS, TimeUnit.SECONDS)
        .thenApply(i1 -> VALIDITY_SECONDS);
  }

  @Override
  public CompletionStage<Optional<Integer>> getCode(UUID userId) {
    return tfaSmsChallengeMap.getAsync(userId).thenApply(Optional::ofNullable);
  }

  @Override
  public void deleteCode(UUID userId) {
    tfaSmsChallengeMap.delete(userId);
  }
}

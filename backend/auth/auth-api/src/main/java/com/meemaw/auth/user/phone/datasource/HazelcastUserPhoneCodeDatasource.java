package com.meemaw.auth.user.phone.datasource;

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

@ApplicationScoped
@Slf4j
public class HazelcastUserPhoneCodeDatasource implements UserPhoneCodeDatasource {

  private static final int VALIDITY_SECONDS = 60;

  private IMap<UUID, Integer> userPhoneCodeMap;

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.user-phone-code-map")
  String userPhoneCodeMapName;

  @PostConstruct
  public void init() {
    log.info("userPhoneCodeMap: {}", userPhoneCodeMapName);
    userPhoneCodeMap = hazelcastProvider.getInstance().getMap(userPhoneCodeMapName);
  }

  @Override
  public CompletionStage<Integer> setCode(UUID userId, int code) {
    return userPhoneCodeMap
        .setAsync(userId, code, VALIDITY_SECONDS, TimeUnit.SECONDS)
        .thenApply(i1 -> VALIDITY_SECONDS);
  }

  @Override
  public CompletionStage<Optional<Integer>> getCode(UUID userId) {
    return userPhoneCodeMap.getAsync(userId).thenApply(Optional::ofNullable);
  }

  @Override
  public void deleteCode(UUID userId) {
    userPhoneCodeMap.delete(userId);
  }
}

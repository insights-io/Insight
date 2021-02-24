package com.rebrowse.auth.user.phone.datasource;

import com.hazelcast.map.IMap;
import com.rebrowse.shared.hazelcast.cdi.HazelcastProvider;
import java.util.Optional;
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

  private IMap<String, Integer> userPhoneCodeMap;

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.user-phone-code-map")
  String userPhoneCodeMapName;

  @PostConstruct
  public void init() {
    log.info("userPhoneCodeMap: {}", userPhoneCodeMapName);
    userPhoneCodeMap = hazelcastProvider.getInstance().getMap(userPhoneCodeMapName);
  }

  @Override
  public CompletionStage<Integer> setCode(String key, int code) {
    return userPhoneCodeMap
        .setAsync(key, code, VALIDITY_SECONDS, TimeUnit.SECONDS)
        .thenApply(ignored -> VALIDITY_SECONDS);
  }

  @Override
  public CompletionStage<Optional<Integer>> getCode(String key) {
    return userPhoneCodeMap.getAsync(key).thenApply(Optional::ofNullable);
  }

  @Override
  public void deleteCode(String key) {
    userPhoneCodeMap.delete(key);
  }
}

package com.rebrowse.auth.mfa.totp.datasource;

import com.hazelcast.map.IMap;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.shared.hazelcast.cdi.HazelcastProvider;
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
public class HazelcastMfaTotpSetupDatasource implements MfaTotpSetupDatasource {

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.mfa-totp-challenge-setup-map")
  String mapName;

  private IMap<UUID, String> map;

  @PostConstruct
  public void init() {
    map = hazelcastProvider.getInstance().getMap(mapName);
  }

  @Override
  public CompletionStage<Optional<String>> retrieve(UUID userId) {
    return map.getAsync(userId).thenApply(Optional::ofNullable);
  }

  @Override
  public CompletionStage<Void> set(UUID userId, String secret) {
    return map.setAsync(userId, secret, AuthorizationPwdChallengeSession.TTL, TimeUnit.SECONDS);
  }

  @Override
  public void delete(UUID userId) {
    map.delete(userId);
  }
}

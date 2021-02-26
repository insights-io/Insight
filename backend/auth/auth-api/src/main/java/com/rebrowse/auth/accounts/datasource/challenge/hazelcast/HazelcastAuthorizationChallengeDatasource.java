package com.rebrowse.auth.accounts.datasource.challenge.hazelcast;

import com.hazelcast.map.IMap;
import com.rebrowse.auth.accounts.datasource.challenge.AuthorizationChallengeDatasource;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationChallenge;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationChallengeType;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.shared.hazelcast.cdi.HazelcastProvider;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class HazelcastAuthorizationChallengeDatasource implements AuthorizationChallengeDatasource {

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.accounts.challenge.datasource.map")
  String mapName;

  private IMap<String, AuthorizationChallenge> store;

  @PostConstruct
  public void init() {
    store = hazelcastProvider.getInstance().getMap(mapName);
  }

  @Override
  public CompletionStage<String> create(AuthorizationChallenge challenge) {
    String id = AuthorizationPwdChallengeSession.newIdentifier();
    return store
        .setAsync(id, challenge, AuthorizationPwdChallengeSession.TTL, TimeUnit.SECONDS)
        .thenApply(oldValue -> id);
  }

  @Override
  public CompletionStage<Optional<AuthorizationChallenge>> retrieve(
      String id, AuthorizationChallengeType type) {
    return store
        .getAsync(id)
        .thenApply(
            maybeChallenge -> {
              if (maybeChallenge == null) {
                return Optional.empty();
              }
              if (!maybeChallenge.getType().equals(type)) {
                return Optional.empty();
              }
              return Optional.of(maybeChallenge);
            });
  }

  @Override
  public void delete(String challengeId) {
    store.delete(challengeId);
  }
}

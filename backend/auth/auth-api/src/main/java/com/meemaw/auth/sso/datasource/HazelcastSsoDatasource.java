package com.meemaw.auth.sso.datasource;

import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.IMap;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.model.SsoUser;
import com.meemaw.auth.user.model.AuthUser;
import io.smallrye.mutiny.Uni;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class HazelcastSsoDatasource implements SsoDatasource {

  private IMap<String, SsoUser> sessionToUserMap;
  private IMap<UUID, Set<String>> userToSessionsMap;

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.session-to-user-map")
  String sessionToUserMapName;

  @ConfigProperty(name = "hazelcast.auth.user-to-sessions-map")
  String userToSessionsMapName;

  @PostConstruct
  public void init() {
    log.info(
        "Initializing HazelcastSsoDatasource sessionToUserMap: {} userToSessionsMap: {}",
        sessionToUserMapName,
        userToSessionsMapName);

    sessionToUserMap = hazelcastProvider.getInstance().getMap(sessionToUserMapName);
    userToSessionsMap = hazelcastProvider.getInstance().getMap(userToSessionsMapName);
  }

  @Override
  @Traced
  public CompletionStage<String> createSession(AuthUser user) {
    String sessionId = SsoSession.newIdentifier();

    CompletionStage<Void> sessionToUserLookup =
        sessionToUserMap.setAsync(
            sessionId,
            SsoUser.as(user),
            SsoSession.TTL,
            TimeUnit.SECONDS,
            SsoSession.MAX_IDLE,
            TimeUnit.SECONDS);

    CompletionStage<Void> userToSessionsLookup =
        userToSessionsMap.submitToKey(
            user.getId(),
            (EntryProcessor<UUID, Set<String>, Void>)
                entry -> {
                  Set<String> sessionIds =
                      Optional.ofNullable(entry.getValue()).orElseGet(HashSet::new);
                  sessionIds.add(sessionId);
                  entry.setValue(sessionIds);
                  return null;
                });

    return Uni.combine()
        .all()
        .unis(
            Uni.createFrom().completionStage(sessionToUserLookup),
            Uni.createFrom().completionStage(userToSessionsLookup))
        .combinedWith(ignored -> sessionId)
        .subscribeAsCompletionStage();
  }

  @Override
  @Traced
  public CompletionStage<Optional<SsoUser>> findSession(String sessionId) {
    return sessionToUserMap.getAsync(sessionId).thenApply(Optional::ofNullable);
  }

  @Override
  @Traced
  public CompletionStage<Optional<SsoUser>> deleteSession(String sessionId) {
    return sessionToUserMap
        .removeAsync(sessionId)
        .thenApply(
            ssoUser -> {
              if (ssoUser == null) {
                return Optional.empty();
              }
              log.info("Session id={} deleted for userId={}", sessionId, ssoUser.getId());
              return Optional.of(ssoUser);
            });
  }

  @Override
  @Traced
  public CompletionStage<Collection<String>> deleteAllSessionsForUser(UUID userId) {
    Set<String> sessions =
        Optional.ofNullable(userToSessionsMap.get(userId)).orElseGet(Collections::emptySet);

    return sessionToUserMap
        .submitToKeys(
            new HashSet<>(sessions),
            (EntryProcessor<String, SsoUser, SsoUser>)
                entry -> {
                  entry.setValue(null);
                  return null;
                })
        .thenApply(ignored -> sessions);
  }
}

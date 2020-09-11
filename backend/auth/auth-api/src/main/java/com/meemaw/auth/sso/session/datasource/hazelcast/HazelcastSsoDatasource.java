package com.meemaw.auth.sso.session.datasource.hazelcast;

import com.hazelcast.map.IMap;
import com.meemaw.auth.sso.model.SsoSession;
import com.meemaw.auth.sso.session.datasource.SsoDatasource;
import com.meemaw.auth.sso.session.model.SsoUser;
import com.meemaw.auth.user.model.AuthUser;
import io.smallrye.mutiny.Uni;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
        userToSessionsMap.submitToKey(user.getId(), new CreateSessionEntryProcessor(sessionId));

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
    SsoUser maybeSsoUser = sessionToUserMap.remove(sessionId);
    if (maybeSsoUser == null) {
      return CompletableFuture.completedStage(Optional.empty());
    }

    return userToSessionsMap
        .submitToKey(maybeSsoUser.getId(), new DeleteSessionEntryProcessor(sessionId))
        .thenApply(ignored -> Optional.of(maybeSsoUser));
  }

  @Override
  @Traced
  public CompletionStage<Set<String>> getAllSessionsForUser(UUID userId) {
    return userToSessionsMap
        .getAsync(userId)
        .thenApply(
            maybeSessions -> Optional.ofNullable(maybeSessions).orElseGet(Collections::emptySet));
  }

  @Override
  @Traced
  public CompletionStage<Set<String>> deleteAllSessionsForUser(UUID userId) {
    return setValueOnAllSessionsForUser(userId, null);
  }

  @Override
  @Traced
  public CompletionStage<Set<String>> updateUserSessions(UUID userId, AuthUser user) {
    return setValueOnAllSessionsForUser(userId, SsoUser.as(user));
  }

  private CompletionStage<Set<String>> setValueOnAllSessionsForUser(UUID userId, SsoUser value) {
    return getAllSessionsForUser(userId)
        .thenCompose(
            sessions ->
                sessionToUserMap
                    .submitToKeys(sessions, new SetUserEntryProcessor(value))
                    .thenApply(ignored -> sessions));
  }
}

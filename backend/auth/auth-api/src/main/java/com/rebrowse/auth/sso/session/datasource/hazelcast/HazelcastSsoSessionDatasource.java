package com.rebrowse.auth.sso.session.datasource.hazelcast;

import com.hazelcast.map.IMap;
import com.rebrowse.auth.sso.session.datasource.SsoSessionDatasource;
import com.rebrowse.auth.sso.session.model.SsoSession;
import com.rebrowse.auth.sso.session.model.SsoUser;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.hazelcast.cdi.HazelcastProvider;
import com.rebrowse.shared.hazelcast.processors.SetValueEntryProcessor;
import io.smallrye.mutiny.Uni;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class HazelcastSsoSessionDatasource implements SsoSessionDatasource {

  @Inject HazelcastProvider hazelcastProvider;

  @ConfigProperty(name = "hazelcast.auth.session-to-user-map")
  String sessionToUserMapName;

  @ConfigProperty(name = "hazelcast.auth.user-to-sessions-map")
  String userToSessionsMapName;

  private IMap<String, SsoUser> sessionToUserMap;
  private IMap<UUID, Set<String>> userToSessionsMap;

  @PostConstruct
  public void init() {
    sessionToUserMap = hazelcastProvider.getInstance().getMap(sessionToUserMapName);
    userToSessionsMap = hazelcastProvider.getInstance().getMap(userToSessionsMapName);
  }

  @Override
  @Traced
  public CompletionStage<String> create(AuthUser user) {
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
  public CompletionStage<Optional<SsoUser>> retrieve(String sessionId) {
    return sessionToUserMap.getAsync(sessionId).thenApply(Optional::ofNullable);
  }

  @Override
  @Traced
  public CompletionStage<Optional<SsoUser>> delete(String sessionId) {
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
  public CompletionStage<Set<String>> listAllForUser(UUID userId) {
    return userToSessionsMap
        .getAsync(userId)
        .thenApply(
            maybeSessions -> Optional.ofNullable(maybeSessions).orElseGet(Collections::emptySet));
  }

  @Override
  @Traced
  public CompletionStage<Set<String>> deleteAllForUser(UUID userId) {
    return setValueOnAllSessionsForUser(userId, null);
  }

  @Override
  @Traced
  public CompletionStage<Set<String>> updateAllForUser(UUID userId, AuthUser user) {
    return setValueOnAllSessionsForUser(userId, SsoUser.as(user));
  }

  @Override
  public CompletionStage<Void> deleteAllForOrganization(String organizationId) {
    Set<Entry<String, SsoUser>> users = retrieveOrganizationUsers(organizationId);
    Set<String> sessionIds = users.stream().map(Entry::getKey).collect(Collectors.toSet());
    Set<UUID> userIds =
        users.stream().map(Entry::getValue).map(SsoUser::getId).collect(Collectors.toSet());

    return Uni.combine()
        .all()
        .unis(
            Uni.createFrom().completionStage(deleteSessionsToUser(sessionIds)),
            Uni.createFrom().completionStage(deleteUsersToSessions(userIds)))
        .discardItems()
        .subscribeAsCompletionStage();
  }

  private Set<Entry<String, SsoUser>> retrieveOrganizationUsers(String organizationId) {
    return sessionToUserMap.entrySet(
        data -> data.getValue().getOrganizationId().equals(organizationId));
  }

  private CompletionStage<Set<String>> setValueOnAllSessionsForUser(UUID userId, SsoUser value) {
    return listAllForUser(userId)
        .thenCompose(
            sessions ->
                sessionToUserMap
                    .submitToKeys(sessions, new SetValueEntryProcessor<>(value))
                    .thenApply(ignored -> sessions));
  }

  private CompletionStage<Map<String, SsoUser>> deleteSessionsToUser(Set<String> sessions) {
    return sessionToUserMap.submitToKeys(sessions, new SetValueEntryProcessor<>(null));
  }

  private CompletionStage<Map<UUID, Set<String>>> deleteUsersToSessions(Set<UUID> users) {
    return userToSessionsMap.submitToKeys(users, new SetValueEntryProcessor<>(null));
  }
}

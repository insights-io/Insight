package com.meemaw.auth.sso.datasource;

import com.meemaw.auth.sso.model.SsoUser;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface SsoDatasource {

  CompletionStage<String> createSession(AuthUser user);

  CompletionStage<Optional<SsoUser>> findSession(String sessionId);

  CompletionStage<Optional<SsoUser>> deleteSession(String sessionId);

  CompletionStage<Set<String>> deleteAllSessionsForUser(UUID userId);

  CompletionStage<Set<String>> getAllSessionsForUser(UUID userId);

  CompletionStage<Set<String>> updateUserSessions(UUID userId, AuthUser user);
}

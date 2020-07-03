package com.meemaw.auth.sso.datasource;

import com.meemaw.auth.sso.model.SsoUser;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface SsoDatasource {

  /**
   * Create a new SSO session for user.
   *
   * @param user auth user
   * @return newly created session id
   */
  CompletionStage<String> createSession(AuthUser user);

  /**
   * Find an existing SSO session if it exists.
   *
   * @param sessionId session id
   * @return maybe user associated with the session if the session exists
   */
  CompletionStage<Optional<SsoUser>> findSession(String sessionId);

  /**
   * Delete an existing SSO session if it exists.
   *
   * @param sessionId session id
   * @return maybe user associated with the session if the session exists
   */
  CompletionStage<Optional<SsoUser>> deleteSession(String sessionId);

  /**
   * Delete all SSO sessions for a given user id.
   *
   * @param userId user id
   * @return collection of deleted sessions
   */
  CompletionStage<Collection<String>> deleteAllSessionsForUser(UUID userId);
}

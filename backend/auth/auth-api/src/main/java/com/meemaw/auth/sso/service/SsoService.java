package com.meemaw.auth.sso.service;

import com.meemaw.auth.sso.model.SsoUser;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface SsoService {

  /**
   * Create a SSO session.
   *
   * @param user auth user to associate session with
   * @return string session ID
   */
  CompletionStage<String> createSession(AuthUser user);

  /**
   * Find an existing SSO session.
   *
   * @param sessionId to look for
   * @return Optional user associated with the session
   */
  CompletionStage<Optional<AuthUser>> findSession(String sessionId);

  /**
   * Find all SSO sessions associated with the same user as sessionId.
   *
   * @param sessionId to look for
   * @return set of sessions
   */
  CompletionStage<Set<String>> findSessions(String sessionId);

  /**
   * Logout an existing SSO session.
   *
   * @param sessionId to look for
   * @return maybe user associated with logout if it was successful.
   */
  CompletionStage<Optional<SsoUser>> logout(String sessionId);

  /**
   * Logout user from all devices.
   *
   * @param userId user id
   * @return set of sessions that has been revoked
   */
  CompletionStage<Set<String>> logoutUserFromAllDevices(UUID userId);

  /**
   * Logout user associated with a session id from all devices.
   *
   * @param sessionId session id
   * @return set of sessions that has been revoked
   */
  default CompletionStage<Set<String>> logoutFromAllDevices(String sessionId) {
    return findSession(sessionId)
        .thenCompose(
            maybeUser ->
                maybeUser.isEmpty()
                    ? CompletableFuture.completedStage(Collections.emptySet())
                    : logoutUserFromAllDevices(maybeUser.get().getId()));
  }

  /**
   * Log user associated with the provided credentials in.
   *
   * @param email of the user
   * @param password of the user
   * @param ipAddress of the user
   * @return String session ID
   */
  CompletionStage<String> login(String email, String password, String ipAddress);

  /**
   * Log user in using social login.
   *
   * @param email of the user
   * @param fullName full name of the user
   * @return String session ID
   */
  CompletionStage<String> socialLogin(String email, String fullName);
}

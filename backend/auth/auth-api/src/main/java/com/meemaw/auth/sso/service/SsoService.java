package com.meemaw.auth.sso.service;

import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
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
   * Logout an existing SSO session.
   *
   * @param sessionId to look for
   * @return boolean indicating whether logout was successful.
   */
  CompletionStage<Boolean> logout(String sessionId);

  /**
   * Log user associated with the provided credentials in.
   *
   * @param email of the user
   * @param password of the user
   * @return String session ID
   */
  CompletionStage<String> login(String email, String password);

  /**
   * Log user in using social login.
   *
   * @param email of the user
   * @param fullName full name of the user
   * @return String session ID
   */
  CompletionStage<String> socialLogin(String email, String fullName);
}

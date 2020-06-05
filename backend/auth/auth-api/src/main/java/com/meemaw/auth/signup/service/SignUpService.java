package com.meemaw.auth.signup.service;

import com.meemaw.auth.signup.model.SignUpRequest;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.tuple.Pair;

public interface SignUpService {

  /**
   * Sign up a new user.
   *
   * @param referer maybe referer to redirect to on sign up completion
   * @param requestBaseURL request base URL
   * @param signUpRequestDTO sign up request
   * @return Optional UUID sign up request token if created
   */
  CompletionStage<Optional<UUID>> signUp(
      String referer, String requestBaseURL, SignUpRequestDTO signUpRequestDTO);

  /**
   * Complete the sign up flow. Creates following models: `auth.user`, `auth.organization`,
   * `auth.password`. Deletes following models: `auth.sign_up_request`.
   *
   * @param token sign up request token
   * @return Pair AuthUser and completed SignUpRequest
   */
  CompletionStage<Pair<AuthUser, SignUpRequest>> completeSignUp(UUID token);

  /**
   * Check if sign up request is valid. Returns false if it doesn't exist.
   *
   * @param token UUID sign up request token
   * @return boolean indicating if sign up request is valid
   */
  CompletionStage<Boolean> signUpRequestValid(UUID token);

  /**
   * Social sign up flow. We get email and fullName from the social provider.
   *
   * @param email address
   * @param fullName full name
   * @return AuthUser newly created user
   */
  CompletionStage<AuthUser> socialSignUp(String email, String fullName);
}

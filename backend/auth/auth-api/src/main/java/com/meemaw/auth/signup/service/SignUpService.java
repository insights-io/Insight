package com.meemaw.auth.signup.service;

import com.meemaw.auth.signup.model.SignUpRequest;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.tuple.Pair;

public interface SignUpService {

  CompletionStage<Optional<UUID>> signUp(
      String referer, String requestBaseURL, SignUpRequestDTO signUpRequestDTO);

  CompletionStage<Pair<AuthUser, SignUpRequest>> completeSignUp(UUID token);

  CompletionStage<Boolean> signUpRequestValid(UUID token);

  CompletionStage<AuthUser> socialSignUp(String email, String fullName);
}

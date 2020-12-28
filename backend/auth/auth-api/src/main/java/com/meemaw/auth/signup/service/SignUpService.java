package com.meemaw.auth.signup.service;

import com.meemaw.auth.signup.model.SignUpRequest;
import com.meemaw.auth.signup.model.dto.SignUpRequestDTO;
import com.meemaw.auth.user.model.AuthUser;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.tuple.Pair;

public interface SignUpService {

  CompletionStage<Optional<UUID>> signUp(
      URL referrer, URL serverBaseURL, SignUpRequestDTO signUpRequestDTO);

  CompletionStage<Pair<AuthUser, SignUpRequest>> completeSignUp(UUID token);

  CompletionStage<Boolean> signUpRequestValid(UUID token);

  CompletionStage<AuthUser> socialSignUp(String email, String fullName);

  CompletionStage<AuthUser> ssoSignUp(String email, String fullName, String organizationId);
}

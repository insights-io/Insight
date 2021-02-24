package com.rebrowse.auth.signup.service;

import com.rebrowse.auth.signup.model.SignUpRequest;
import com.rebrowse.auth.signup.model.dto.SignUpRequestDTO;
import com.rebrowse.auth.user.model.AuthUser;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.tuple.Pair;

public interface SignUpService {

  CompletionStage<Optional<UUID>> signUp(URI serverBaseUri, SignUpRequestDTO signUpRequestDTO);

  CompletionStage<Pair<AuthUser, SignUpRequest>> completeSignUp(UUID token);

  CompletionStage<Boolean> signUpRequestValid(UUID token);

  CompletionStage<AuthUser> ssoSignUp(String email, String fullName);

  CompletionStage<AuthUser> ssoSignUpNewMember(
      String email, String fullName, String organizationId);
}

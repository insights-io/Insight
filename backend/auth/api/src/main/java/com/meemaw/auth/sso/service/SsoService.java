package com.meemaw.auth.sso.service;

import com.meemaw.shared.auth.UserDTO;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface SsoService {

  CompletionStage<String> createSession(UserDTO user);

  CompletionStage<Optional<UserDTO>> findSession(String sessionId);

  CompletionStage<Boolean> logout(String sessionId);

  CompletionStage<String> login(String email, String password);

  CompletionStage<String> socialLogin(String email);
}

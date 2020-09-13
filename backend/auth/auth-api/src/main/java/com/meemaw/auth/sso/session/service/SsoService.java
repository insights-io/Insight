package com.meemaw.auth.sso.session.service;

import com.meemaw.auth.sso.session.model.LoginMethod;
import com.meemaw.auth.sso.session.model.LoginResult;
import com.meemaw.auth.sso.session.model.SsoUser;
import com.meemaw.auth.user.model.AuthUser;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface SsoService {

  CompletionStage<String> createSession(AuthUser user);

  CompletionStage<String> createSession(UUID userId);

  CompletionStage<Optional<AuthUser>> findSession(String sessionId);

  CompletionStage<Set<String>> findSessions(String sessionId);

  CompletionStage<Optional<SsoUser>> logout(String sessionId);

  CompletionStage<Set<String>> logoutUserFromAllDevices(UUID userId);

  default CompletionStage<Set<String>> logoutFromAllDevices(String sessionId) {
    return findSession(sessionId)
        .thenCompose(
            maybeUser ->
                maybeUser.isEmpty()
                    ? CompletableFuture.completedStage(Collections.emptySet())
                    : logoutUserFromAllDevices(maybeUser.get().getId()));
  }

  CompletionStage<LoginResult<?>> passwordLogin(
      String email, String password, String ipAddress, URL redirect, URI serverBase);

  CompletionStage<LoginResult<?>> socialLogin(
      String email, String fullName, LoginMethod loginMethod, URL redirect, URI serverBase);

  CompletionStage<LoginResult<?>> ssoLogin(
      String email, String fullName, String organizationId, URL redirect);
}

package com.rebrowse.auth.password.service;

import com.rebrowse.auth.accounts.datasource.challenge.AuthorizationChallengeDatasource;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationChallenge;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationChallengeType;
import com.rebrowse.auth.accounts.model.challenge.PwdChallengeResponseDTO;
import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.AuthorizationResponse;
import com.rebrowse.auth.sso.session.service.SsoService;
import com.rebrowse.shared.rest.response.Boom;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PwdAuthorizationChallengeService {

  @Inject AuthorizationChallengeDatasource authorizationChallengeDatasource;
  @Inject PasswordService passwordService;
  @Inject
  SsoService ssoService;

  public CompletionStage<AuthorizationResponse> completeChallenge(
      String challengeId, String email, String password, URI serverBaseUri) {
    return authorizationChallengeDatasource
        .retrieve(challengeId, AuthorizationChallengeType.PASSWORD)
        .thenCompose(
            maybePasswordChallenge -> {
              AuthorizationChallenge passwordChallenge =
                  maybePasswordChallenge.orElseThrow(() -> Boom.unauthorized().exception());

              String challengeEmail = passwordChallenge.getValue();
              if (!challengeEmail.equals(email)) {
                throw Boom.unauthorized().exception();
              }

              URI redirect = passwordChallenge.getRedirect();
              AuthorizationRequest authorizationRequest =
                  new AuthorizationRequest(email, redirect, serverBaseUri);

              return passwordService
                  .verifyPassword(email, password)
                  .thenCompose(
                      user -> {
                        authorizationChallengeDatasource.delete(challengeId);
                        return ssoService.authorize(user, authorizationRequest);
                      });
            });
  }

  public CompletionStage<String> createChallenge(
      String email, AuthorizationRequest authorizationRequest) {
    AuthorizationChallenge challenge =
        AuthorizationChallenge.password(email, authorizationRequest.getRedirect());
    return authorizationChallengeDatasource.create(challenge);
  }

  public CompletionStage<Optional<PwdChallengeResponseDTO>> retrieveChallenge(String challengeId) {
    return authorizationChallengeDatasource
        .retrieve(challengeId, AuthorizationChallengeType.PASSWORD)
        .thenApply(
            (maybeChallenge) ->
                maybeChallenge.map(
                    challenge ->
                        new PwdChallengeResponseDTO(
                            challenge.getRedirect(), challenge.getValue())));
  }
}

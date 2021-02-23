package com.rebrowse.auth.accounts.service;

import com.rebrowse.auth.accounts.model.request.AuthorizationRequest;
import com.rebrowse.auth.accounts.model.response.ChooseAccountPwdChallengeResponse;
import com.rebrowse.auth.accounts.model.response.ChooseAccountResponse;
import com.rebrowse.auth.accounts.model.response.ChooseAccountSsoRedirectResponse;
import com.rebrowse.auth.core.EmailUtils;
import com.rebrowse.auth.password.service.PwdAuthorizationChallengeService;
import com.rebrowse.auth.sso.IdentityProviderRegistry;
import com.rebrowse.auth.sso.setup.datasource.SsoSetupDatasource;
import com.rebrowse.auth.sso.setup.model.dto.SsoSetupDTO;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AccountsService {

  @Inject
  PwdAuthorizationChallengeService pwdAuthorizationChallengeService;
  @Inject
  SsoSetupDatasource ssoSetupDatasource;
  @Inject
  IdentityProviderRegistry identityProviderRegistry;

  @Context HttpServerRequest request;
  @Context UriInfo info;

  public CompletionStage<ChooseAccountResponse> chooseAccount(
      AuthorizationRequest authorizationRequest) {
    String email = authorizationRequest.getEmail();
    String emailDomain = EmailUtils.getDomain(email);

    // TODO: what if Organization has an external user with non business domain?
    // SSO login is not supported for non business emails
    if (!EmailUtils.isBusinessDomain(emailDomain)) {
      return createChallenge(email, authorizationRequest);
    }

    return ssoSetupDatasource
        .getByDomain(emailDomain)
        .thenCompose(
            maybeSsoSetup -> {
              // Fallback to pwd challenge if no SSO setup associated with domain
              if (maybeSsoSetup.isEmpty()) {
                return createChallenge(email, authorizationRequest);
              }

              SsoSetupDTO ssoSetup = maybeSsoSetup.get();
              return ssoAuthorizationRedirect(ssoSetup, authorizationRequest);
            });
  }

  private CompletionStage<ChooseAccountResponse> ssoAuthorizationRedirect(
      SsoSetupDTO ssoSetup, AuthorizationRequest request) {
    return identityProviderRegistry
        .getService(ssoSetup.getMethod())
        .getSsoAuthorizationRequest(ssoSetup, request)
        .thenApply(
            (ssoRequest) -> new ChooseAccountSsoRedirectResponse(ssoRequest, request.getDomain()));
  }

  private CompletionStage<ChooseAccountResponse> createChallenge(
      String email, AuthorizationRequest authorizationRequest) {
    return pwdAuthorizationChallengeService
        .createChallenge(email, authorizationRequest)
        .thenApply(
            challengeId ->
                new ChooseAccountPwdChallengeResponse(
                    challengeId, authorizationRequest.getDomain()));
  }
}

package com.meemaw.auth.mfa.setup.resource.v1;

import com.meemaw.auth.mfa.MfaMethod;
import com.meemaw.auth.mfa.model.MfaConfiguration;
import com.meemaw.auth.mfa.model.SsoChallenge;
import com.meemaw.auth.mfa.model.dto.MfaChallengeCompleteDTO;
import com.meemaw.auth.mfa.setup.service.MfaSetupService;
import com.meemaw.auth.mfa.sms.impl.MfaSmsProvider;
import com.meemaw.auth.sso.session.model.AuthPrincipal;
import com.meemaw.auth.sso.session.service.SsoService;
import com.meemaw.auth.user.datasource.UserMfaDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.shared.context.RequestUtils;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import io.vertx.core.http.HttpServerRequest;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class MfaSetupResourceImpl implements MfaSetupResource {

  @Inject AuthPrincipal principal;
  @Inject MfaSetupService mfaSetupService;
  @Inject UserMfaDatasource userMfaDatasource;
  @Inject MfaSmsProvider smsProvider;
  @Inject SsoService ssoService;
  @Context HttpServerRequest request;
  @Context UriInfo uriInfo;

  @Override
  public CompletionStage<Response> list() {
    AuthUser user = principal.user();
    return userMfaDatasource
        .list(user.getId())
        .thenApply(
            setups -> setups.stream().map(MfaConfiguration::dto).collect(Collectors.toList()))
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> retrieve(MfaMethod method) {
    AuthUser user = principal.user();
    return userMfaDatasource
        .retrieve(user.getId(), method)
        .thenApply(
            maybeConfiguration -> {
              if (maybeConfiguration.isEmpty()) {
                return Boom.notFound().response();
              }
              return DataResponse.ok(maybeConfiguration.get().dto());
            });
  }

  @Override
  public CompletionStage<Response> delete(MfaMethod method) {
    AuthUser user = principal.user();
    return mfaSetupService
        .mfaSetupDisable(user.getId(), method)
        .thenApply(ignored -> DataResponse.noContent());
  }

  @Override
  public CompletionStage<Response> start(MfaMethod method) {
    return mfaSetupService.mfaSetupStart(method, principal).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> complete(MfaMethod method, MfaChallengeCompleteDTO body) {
    AuthUser user = principal.user();
    return mfaSetupService
        .mfaSetupComplete(method, user, body.getCode())
        .thenApply(data -> DataResponse.ok(data.getLeft().dto()));
  }

  @Override
  public CompletionStage<Response> completeEnforcedSetup(
      MfaMethod method, MfaChallengeCompleteDTO body) {
    URL serverBaseUrl = RequestUtils.getServerBaseUrl(uriInfo, request);
    String cookieDomain = RequestUtils.parseCookieDomain(serverBaseUrl);

    return mfaSetupService
        .mfaSetupComplete(method, principal.user(), body.getCode())
        .thenCompose(
            data ->
                ssoService
                    .authenticateDirect(data.getRight())
                    .thenApply(
                        loginResult ->
                            DataResponse.okBuilder(loginResult.getData())
                                .cookie(
                                    loginResult.loginCookie(cookieDomain),
                                    SsoChallenge.clearCookie(cookieDomain))
                                .build()));
  }

  @Override
  public CompletionStage<Response> sendSmsCode() {
    AuthUser user = principal.user();
    PhoneNumber phoneNumber = user.getPhoneNumber();
    String setupCodeKey = MfaSmsProvider.setupCodeKey(principal);
    return smsProvider.sendVerificationCode(setupCodeKey, phoneNumber).thenApply(DataResponse::ok);
  }
}

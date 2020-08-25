package com.meemaw.auth.tfa.setup.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.auth.tfa.TfaMethod;
import com.meemaw.auth.tfa.challenge.model.dto.TfaChallengeCompleteDTO;
import com.meemaw.auth.tfa.setup.model.TfaSetup;
import com.meemaw.auth.tfa.setup.service.TfaSetupService;
import com.meemaw.auth.user.datasource.UserTfaDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class TfaResourceImpl implements TfaResource {

  @Inject InsightPrincipal principal;
  @Inject TfaSetupService tfaSetupService;
  @Inject UserTfaDatasource userTfaDatasource;

  @Override
  public CompletionStage<Response> list() {
    AuthUser user = principal.user();
    return userTfaDatasource
        .list(user.getId())
        .thenApply(setups -> setups.stream().map(TfaSetup::dto).collect(Collectors.toList()))
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> get(TfaMethod method) {
    AuthUser user = principal.user();
    return userTfaDatasource
        .get(user.getId(), method)
        .thenApply(
            maybeTfaSetup -> {
              if (maybeTfaSetup.isEmpty()) {
                return Boom.notFound().response();
              }
              return DataResponse.ok(maybeTfaSetup.get().dto());
            });
  }

  @Override
  public CompletionStage<Response> delete(TfaMethod method) {
    AuthUser user = principal.user();
    return tfaSetupService.tfaSetupDisable(user.getId(), method).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> tfaSetupStart(TfaMethod method) {
    AuthUser user = principal.user();
    return tfaSetupService
        .tfaSetupStart(method, user.getId(), user.getEmail())
        .thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> tfaSetupComplete(
      TfaMethod method, TfaChallengeCompleteDTO body) {
    AuthUser user = principal.user();
    return tfaSetupService
        .tfaSetupComplete(method, user.getId(), body.getCode())
        .thenApply(tfaSetup -> DataResponse.ok(tfaSetup.dto()));
  }
}

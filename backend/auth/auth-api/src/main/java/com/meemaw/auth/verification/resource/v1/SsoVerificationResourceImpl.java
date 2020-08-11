package com.meemaw.auth.verification.resource.v1;

import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.auth.sso.model.TFASetupCompleteDTO;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.verification.service.TfaService;
import com.meemaw.shared.rest.response.DataResponse;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsoVerificationResourceImpl implements SsoVerificationResource {

  @Inject InsightPrincipal principal;
  @Inject TfaService tfaService;

  @Override
  public CompletionStage<Response> tfaSetupStart() {
    AuthUser user = principal.user();
    return tfaService.tfaSetupStart(user.getId(), user.getEmail()).thenApply(DataResponse::ok);
  }

  @Override
  public CompletionStage<Response> tfaSetupComplete(TFASetupCompleteDTO body) {
    AuthUser user = principal.user();
    return tfaService
        .tfaComplete(user.getId(), body.getCode())
        .thenApply(tfaSetup -> DataResponse.ok(true));
  }
}

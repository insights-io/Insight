package com.rebrowse.auth.accounts.model.response;

import com.rebrowse.auth.accounts.model.ChooseAccountSsoRedirectResponseDTO;
import com.rebrowse.auth.accounts.model.SsoAuthorizationSession;
import com.rebrowse.auth.accounts.model.request.SsoAuthorizationRequest;
import com.rebrowse.shared.rest.response.DataResponse;
import javax.ws.rs.core.Response;
import lombok.Value;

@Value
public class ChooseAccountSsoRedirectResponse implements ChooseAccountResponse {

  SsoAuthorizationRequest request;
  String domain;

  @Override
  public Response build() {
    return DataResponse.okBuilder(new ChooseAccountSsoRedirectResponseDTO(request.getLocation()))
        .cookie(SsoAuthorizationSession.cookie(request.getState(), domain))
        .build();
  }
}

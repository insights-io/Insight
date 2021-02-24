package com.rebrowse.auth.accounts.model.response;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationPwdChallengeSession;
import com.rebrowse.auth.accounts.model.challenge.ChooseAccountPwdChallengeResponseDTO;
import com.rebrowse.shared.rest.response.DataResponse;
import javax.ws.rs.core.Response;
import lombok.Value;

@Value
public class ChooseAccountPwdChallengeResponse implements ChooseAccountResponse {

  String challengeId;
  String domain;

  @Override
  public Response build() {
    return DataResponse.okBuilder(new ChooseAccountPwdChallengeResponseDTO())
        .cookie(AuthorizationPwdChallengeSession.cookie(challengeId, domain))
        .build();
  }
}

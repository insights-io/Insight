package com.rebrowse.auth.accounts.model.response;

import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeResponseDTO;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationMfaChallengeSession;
import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.shared.rest.response.DataResponse;
import java.util.List;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import lombok.Value;
import org.apache.commons.lang3.ArrayUtils;

@Value
public class AuthorizationMfaChallengeDataResponse implements AuthorizationResponse {

  String challengeId;
  String domain;
  List<MfaMethod> methods;

  @Override
  public Response response(NewCookie... cookies) {
    return DataResponse.okBuilder(new AuthorizationMfaChallengeResponseDTO(challengeId, methods))
        .cookie(
            ArrayUtils.add(cookies, AuthorizationMfaChallengeSession.cookie(challengeId, domain)))
        .build();
  }
}

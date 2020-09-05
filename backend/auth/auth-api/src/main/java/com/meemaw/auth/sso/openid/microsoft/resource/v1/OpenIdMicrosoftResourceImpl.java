package com.meemaw.auth.sso.openid.microsoft.resource.v1;

import com.meemaw.auth.sso.openid.microsoft.OpenIdMicrosoftService;
import com.meemaw.auth.sso.openid.microsoft.model.MicrosoftErrorResponse;
import com.meemaw.auth.sso.openid.microsoft.model.MicrosoftTokenResponse;
import com.meemaw.auth.sso.openid.microsoft.model.MicrosoftUserInfoResponse;
import com.meemaw.auth.sso.openid.shared.AbstractOpenIdResource;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class OpenIdMicrosoftResourceImpl
    extends AbstractOpenIdResource<
        MicrosoftTokenResponse, MicrosoftUserInfoResponse, MicrosoftErrorResponse>
    implements OpenIdMicrosoftResource {

  @Inject OpenIdMicrosoftService openIdMicrosoftService;

  @Override
  public String getBasePath() {
    return PATH;
  }

  @Override
  public Response signIn(String destination) {
    return signIn(openIdMicrosoftService, destination);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String state, String code, String sessionState) {
    return oauth2callback(openIdMicrosoftService, state, sessionState, code);
  }
}

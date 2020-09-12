package com.meemaw.auth.sso.oauth.microsoft.resource.v1;

import com.meemaw.auth.sso.oauth.microsoft.OAuth2MicrosoftService;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftErrorResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftTokenResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftUserInfoResponse;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuth2Resource;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class OAuth2MicrosoftResourceImpl
    extends AbstractOAuth2Resource<
        MicrosoftTokenResponse, MicrosoftUserInfoResponse, MicrosoftErrorResponse>
    implements OAuth2MicrosoftResource {

  @Inject OAuth2MicrosoftService OAuth2MicrosoftService;

  @Override
  public Response signIn(String destination) {
    return signIn(OAuth2MicrosoftService, destination);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String code, String state, String sessionState) {
    return oauth2callback(OAuth2MicrosoftService, code, state, sessionState);
  }
}

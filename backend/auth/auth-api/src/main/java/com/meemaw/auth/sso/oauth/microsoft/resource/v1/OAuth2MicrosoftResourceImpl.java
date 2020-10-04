package com.meemaw.auth.sso.oauth.microsoft.resource.v1;

import com.meemaw.auth.sso.oauth.microsoft.OAuth2MicrosoftService;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftErrorResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftTokenResponse;
import com.meemaw.auth.sso.oauth.microsoft.model.MicrosoftUserInfoResponse;
import com.meemaw.auth.sso.oauth.shared.AbstractOAuth2Resource;
import java.net.URL;
import java.util.concurrent.CompletionStage;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class OAuth2MicrosoftResourceImpl
    extends AbstractOAuth2Resource<
        MicrosoftTokenResponse, MicrosoftUserInfoResponse, MicrosoftErrorResponse>
    implements OAuth2MicrosoftResource {

  @Inject OAuth2MicrosoftService OAuth2MicrosoftService;

  @Override
  public CompletionStage<Response> signIn(URL redirect, @Nullable String email) {
    return signIn(OAuth2MicrosoftService, redirect, email);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String code, String state, String sessionState) {
    return oauth2callback(OAuth2MicrosoftService, code, state, sessionState);
  }
}

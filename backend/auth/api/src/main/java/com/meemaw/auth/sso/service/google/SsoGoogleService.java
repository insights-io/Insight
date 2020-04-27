package com.meemaw.auth.sso.service.google;

import com.meemaw.auth.sso.model.SsoSocialLogin;
import java.net.URI;
import java.util.concurrent.CompletionStage;

public interface SsoGoogleService {

  URI buildAuthorizationURI(String state, String redirectURI);

  String secureState(String destination);

  CompletionStage<SsoSocialLogin> oauth2callback(String state, String sessionState, String code,
      String redirectURI);
}

package com.rebrowse.auth.utils;

import com.rebrowse.api.RebrowseApiDataResponse;
import com.rebrowse.auth.accounts.model.AuthorizationSuccessResponseDTO;
import com.rebrowse.shared.context.RequestUtils;
import com.rebrowse.test.utils.GlobalTestData;
import java.net.URI;
import java.net.URL;

public final class AuthApiTestData {

  public static final URL OKTA_METADATA_ENDPOINT =
      RequestUtils.sneakyUrl(
          "https://snuderlstest.okta.com/app/exkligrqDovHJsGmk5d5/sso/saml/metadata");

  public static String OKTA_AUTHORIZE_ENDPOINT_PATTERN =
      "^https:\\/\\/snuderlstest\\.okta\\.com\\/app\\/snuderlsorg2948061_rebrowse_2\\/exkligrqDovHJsGmk5d5\\/sso\\/saml\\?RelayState=(.*)http%3A%2F%2Flocalhost%3A3000%2Ftest$";

  public static RebrowseApiDataResponse<AuthorizationSuccessResponseDTO>
      LOCALHOST_AUTHORIZATION_SUCCESS_RESPONSE =
          new RebrowseApiDataResponse<>(
              new AuthorizationSuccessResponseDTO(URI.create(GlobalTestData.LOCALHOST_REDIRECT)));

  private AuthApiTestData() {}
}

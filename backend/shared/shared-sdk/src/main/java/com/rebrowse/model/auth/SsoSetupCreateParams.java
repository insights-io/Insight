package com.rebrowse.model.auth;

import com.rebrowse.model.ApiRequestParams;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SsoSetupCreateParams implements ApiRequestParams {

  SsoMethod method;
  SamlConfiguration saml;

  public static SsoSetupCreateParams saml(SamlConfiguration saml) {
    return new SsoSetupCreateParams(SsoMethod.SAML, saml);
  }

  public static SsoSetupCreateParams google() {
    return new SsoSetupCreateParams(SsoMethod.GOOGLE, null);
  }

  public static SsoSetupCreateParams github() {
    return new SsoSetupCreateParams(SsoMethod.GITHUB, null);
  }

  public static SsoSetupCreateParams microsoft() {
    return new SsoSetupCreateParams(SsoMethod.MICROSOFT, null);
  }
}

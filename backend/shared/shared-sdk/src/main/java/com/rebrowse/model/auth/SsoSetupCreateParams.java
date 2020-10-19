package com.rebrowse.model.auth;

import com.rebrowse.model.ApiRequestParams;
import java.net.URL;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SsoSetupCreateParams implements ApiRequestParams {

  SsoMethod method;
  URL configurationEndpoint;

  public static SsoSetupCreateParams google() {
    return new SsoSetupCreateParams(SsoMethod.GOOGLE, null);
  }

  public static SsoSetupCreateParams microsoft() {
    return new SsoSetupCreateParams(SsoMethod.MICROSOFT, null);
  }

  public static SsoSetupCreateParams github() {
    return new SsoSetupCreateParams(SsoMethod.GITHUB, null);
  }
}

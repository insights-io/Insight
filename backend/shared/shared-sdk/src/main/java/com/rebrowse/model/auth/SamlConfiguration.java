package com.rebrowse.model.auth;

import com.rebrowse.model.ApiRequestParams;
import java.net.URL;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class SamlConfiguration implements ApiRequestParams {

  SamlMethod method;
  URL metadataEndpoint;

  public static SamlConfiguration okta(URL metadataEndpoint) {
    return new SamlConfiguration(SamlMethod.OKTA, metadataEndpoint);
  }
}

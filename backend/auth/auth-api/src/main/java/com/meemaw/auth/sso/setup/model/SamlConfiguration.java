package com.meemaw.auth.sso.setup.model;

import java.net.URL;
import lombok.Value;

@Value
public class SamlConfiguration {

  SamlMethod method;
  URL metadataEndpoint;
}

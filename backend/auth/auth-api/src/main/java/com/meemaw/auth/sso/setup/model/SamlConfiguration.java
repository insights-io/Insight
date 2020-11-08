package com.meemaw.auth.sso.setup.model;

import lombok.Value;

import java.net.URL;

@Value
public class SamlConfiguration {

  SamlMethod method;
  URL metadataEndpoint;
}

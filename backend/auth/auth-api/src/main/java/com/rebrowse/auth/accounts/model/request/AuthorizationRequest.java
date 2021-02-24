package com.rebrowse.auth.accounts.model.request;

import com.rebrowse.shared.context.URIUtils;
import java.net.URI;
import lombok.Value;

@Value
public class AuthorizationRequest {

  String email;
  URI redirect;
  URI serverBaseUri;

  public String getDomain() {
    return URIUtils.parseCookieDomain(serverBaseUri);
  }
}

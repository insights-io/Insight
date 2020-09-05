package com.meemaw.auth.sso.openid.google.resource.v1;

import com.meemaw.auth.sso.openid.shared.OpenIdResource;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import javax.ws.rs.Path;

@Path(OpenIdGoogleResource.PATH)
public interface OpenIdGoogleResource extends OpenIdResource {

  String PATH = SsoResource.PATH + "/google";
}

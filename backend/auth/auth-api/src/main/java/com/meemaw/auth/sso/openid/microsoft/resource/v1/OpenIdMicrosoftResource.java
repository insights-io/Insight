package com.meemaw.auth.sso.openid.microsoft.resource.v1;

import com.meemaw.auth.sso.openid.shared.OpenIdResource;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import javax.ws.rs.Path;

@Path(OpenIdMicrosoftResource.PATH)
public interface OpenIdMicrosoftResource extends OpenIdResource {

  String PATH = SsoResource.PATH + "/microsoft";
}

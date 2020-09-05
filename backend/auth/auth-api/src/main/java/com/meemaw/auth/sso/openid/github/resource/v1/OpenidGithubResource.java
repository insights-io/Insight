package com.meemaw.auth.sso.openid.github.resource.v1;

import com.meemaw.auth.sso.openid.shared.OpenIdResource;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import javax.ws.rs.Path;

@Path(OpenidGithubResource.PATH)
public interface OpenidGithubResource extends OpenIdResource {

  String PATH = SsoResource.PATH + "/github";
}

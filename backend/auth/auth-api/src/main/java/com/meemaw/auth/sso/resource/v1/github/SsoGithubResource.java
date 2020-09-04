package com.meemaw.auth.sso.resource.v1.github;

import com.meemaw.auth.sso.resource.v1.SsoOAuthResource;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import javax.ws.rs.Path;

@Path(SsoGithubResource.PATH)
public interface SsoGithubResource extends SsoOAuthResource {

  String PATH = SsoResource.PATH + "/github";

  @Override
  default String getBasePath() {
    return PATH;
  }
}

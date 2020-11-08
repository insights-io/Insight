package com.meemaw.auth.sso.oauth.github.resource.v1;

import com.meemaw.auth.sso.oauth.OAuthResource;
import javax.ws.rs.Path;

@Path(GithubOAuthResource.PATH)
public interface GithubOAuthResource extends OAuthResource {

  String PATH = OAuthResource.PATH + "/github";
}

package com.rebrowse.auth.sso.oauth.github.resource.v1;

import com.rebrowse.auth.sso.oauth.OAuthResource;
import javax.ws.rs.Path;

@Path(GithubOAuthResource.PATH)
public interface GithubOAuthResource extends OAuthResource {

  String PATH = OAuthResource.PATH + "/github";
}

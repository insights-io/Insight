package com.meemaw.auth.sso.oauth.github.resource.v1;

import com.meemaw.auth.sso.oauth.shared.OAuth2Resource;
import javax.ws.rs.Path;

@Path(OAuth2GithubResource.PATH)
public interface OAuth2GithubResource extends OAuth2Resource {

  String PATH = OAuth2Resource.PATH + "/github";
}

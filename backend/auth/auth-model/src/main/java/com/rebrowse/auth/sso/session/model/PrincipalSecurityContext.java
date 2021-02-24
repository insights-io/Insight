package com.rebrowse.auth.sso.session.model;

import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.UserRole;
import java.security.Principal;
import javax.ws.rs.core.SecurityContext;
import lombok.Value;

@Value
public class PrincipalSecurityContext implements SecurityContext {

  Principal userPrincipal;
  UserRole userRole;
  boolean secure;

  public PrincipalSecurityContext(AuthUser ssoUser, boolean isSecure) {
    this.userRole = ssoUser.getRole();
    this.userPrincipal = () -> ssoUser.getId().toString();
    this.secure = isSecure;
  }

  @Override
  public boolean isUserInRole(String role) {
    return UserRole.fromString(role).equals(userRole);
  }

  @Override
  public String getAuthenticationScheme() {
    return "Cookie-Based-Auth-Scheme";
  }
}

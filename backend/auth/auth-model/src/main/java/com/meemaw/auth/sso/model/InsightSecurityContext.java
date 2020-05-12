package com.meemaw.auth.sso.model;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import java.security.Principal;
import javax.ws.rs.core.SecurityContext;
import lombok.Value;

@Value
public class InsightSecurityContext implements SecurityContext {

  Principal userPrincipal;
  UserRole userRole;
  boolean secure;

  public InsightSecurityContext(AuthUser ssoUser, boolean isSecure) {
    this.userRole = ssoUser.getRole();
    this.userPrincipal = () -> ssoUser.getId().toString();
    this.secure = isSecure;
  }

  @Override
  public boolean isUserInRole(String role) {
    return UserRole.valueOf(role).equals(userRole);
  }

  @Override
  public String getAuthenticationScheme() {
    return "Cookie-Based-Auth-Scheme";
  }
}

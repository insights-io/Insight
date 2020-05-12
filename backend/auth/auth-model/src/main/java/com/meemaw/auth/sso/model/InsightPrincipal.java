package com.meemaw.auth.sso.model;

import com.meemaw.auth.user.model.AuthUser;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import lombok.Getter;

@RequestScoped
@Getter
public class InsightPrincipal {

  private UUID userId;
  private String org;

  /**
   * @param authUser
   * @return
   */
  public InsightPrincipal as(AuthUser authUser) {
    userId = authUser.getId();
    org = authUser.getOrg();
    return this;
  }
}

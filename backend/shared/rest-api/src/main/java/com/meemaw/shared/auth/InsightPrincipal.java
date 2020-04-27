package com.meemaw.shared.auth;

import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import lombok.Getter;

@RequestScoped
@Getter
public class InsightPrincipal {

  private UUID userId;
  private String org;

  public InsightPrincipal as(AuthUser authUser) {
    userId = authUser.getId();
    org = authUser.getOrg();
    return this;
  }

}

package com.meemaw.auth.permissions;

import static com.meemaw.auth.user.UserRegistry.S2S_INTERNAL_USER;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.rest.response.Boom;

public final class AccessManager {

  private AccessManager() {}

  public static void assertCanReadOrganization(AuthUser user, String organizationId) {
    if (!user.getOrganizationId().equals(S2S_INTERNAL_USER.getOrganizationId())
        && !user.getOrganizationId().equals(organizationId)) {
      throw Boom.notFound().exception();
    }
  }
}

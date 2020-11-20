package com.meemaw.auth.user;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.dto.UserDTO;
import com.meemaw.shared.SharedConstants;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class UserRegistry {

  public static final AuthUser S2S_INTERNAL_USER =
      new UserDTO(
          UUID.randomUUID(),
          String.format("internal-s2s@%s", SharedConstants.REBROWSE_STAGING_DOMAIN),
          "Internal S2S User",
          UserRole.ADMIN,
          "internal-s2s",
          OffsetDateTime.parse("2020-12-03T10:15:30+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
          OffsetDateTime.parse("2020-12-03T10:15:30+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
          null,
          false);

  private UserRegistry() {}
}

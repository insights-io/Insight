package com.rebrowse.auth.user.model.dto;

import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.UserRole;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UserDTO implements AuthUser {

  UUID id;
  String email;
  String fullName;
  UserRole role;
  String organizationId;
  OffsetDateTime createdAt;
  OffsetDateTime updatedAt;
  PhoneNumberDTO phoneNumber;
  boolean phoneNumberVerified;
}

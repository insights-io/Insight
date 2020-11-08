package com.meemaw.auth.user.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

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

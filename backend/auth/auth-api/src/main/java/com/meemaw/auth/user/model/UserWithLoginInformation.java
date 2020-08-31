package com.meemaw.auth.user.model;

import com.meemaw.auth.tfa.TfaMethod;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Value;

@Value
public class UserWithLoginInformation {

  UUID id;
  String email;
  String fullName;
  UserRole role;
  String organizationId;
  OffsetDateTime createdAt;
  OffsetDateTime updatedAt;
  PhoneNumber phoneNumber;
  boolean phoneNumberVerified;
  String password;
  List<TfaMethod> tfaMethods;

  public AuthUser user() {
    return new UserDTO(
        id,
        email,
        fullName,
        role,
        organizationId,
        createdAt,
        updatedAt,
        (PhoneNumberDTO) phoneNumber,
        phoneNumberVerified);
  }

  public static UserWithLoginInformation fresh(AuthUser user) {
    return new UserWithLoginInformation(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getRole(),
        user.getOrganizationId(),
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getPhoneNumber(),
        user.isPhoneNumberVerified(),
        null,
        Collections.emptyList());
  }
}

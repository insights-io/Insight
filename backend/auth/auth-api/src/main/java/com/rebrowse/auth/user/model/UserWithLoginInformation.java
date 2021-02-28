package com.rebrowse.auth.user.model;

import com.rebrowse.auth.mfa.MfaMethod;
import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.auth.user.model.dto.UserDTO;
import java.time.OffsetDateTime;
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
  List<MfaMethod> mfaMethods;

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
}

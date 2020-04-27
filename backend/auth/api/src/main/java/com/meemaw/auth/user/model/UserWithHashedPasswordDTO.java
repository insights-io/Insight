package com.meemaw.auth.user.model;

import com.meemaw.shared.auth.UserDTO;
import com.meemaw.shared.auth.UserRole;
import java.util.UUID;
import lombok.Value;

@Value
public class UserWithHashedPasswordDTO {

  UUID id;
  String email;
  UserRole role;
  String org;
  String password;

  public UserDTO user() {
    return new UserDTO(id, email, role, org);
  }
}

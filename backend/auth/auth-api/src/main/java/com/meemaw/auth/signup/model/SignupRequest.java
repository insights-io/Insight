package com.meemaw.auth.signup.model;

import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class SignupRequest {

  String email;
  String org;
  UUID userId;

  public SignupRequest(String email) {
    this.email = email;
  }
}

package com.meemaw.auth.signup.model;

import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class SignupRequest {

  public SignupRequest(String email) {
    this.email = email;
  }

  String email;
  String org;
  UUID userId;
}

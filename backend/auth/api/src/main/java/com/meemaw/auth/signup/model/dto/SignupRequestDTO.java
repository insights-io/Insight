package com.meemaw.auth.signup.model.dto;

import com.meemaw.auth.shared.CanExpire;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class SignupRequestDTO implements CanExpire {

  String email;
  String org;
  UUID token;
  UUID userId;
  OffsetDateTime createdAt;

}

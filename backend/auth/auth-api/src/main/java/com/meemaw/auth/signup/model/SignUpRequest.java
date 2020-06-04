package com.meemaw.auth.signup.model;

import com.meemaw.auth.shared.CanExpire;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class SignUpRequest implements CanExpire {

  UUID token;
  String email;
  String hashedPassword;
  String fullName;
  String company;
  String phoneNumber;
  String referer;
  OffsetDateTime createdAt;

  public Optional<String> getRefererCallbackURL() {
    return Optional.ofNullable(referer).map(r -> String.join("/", r, "signup-completed-callback"));
  }
}

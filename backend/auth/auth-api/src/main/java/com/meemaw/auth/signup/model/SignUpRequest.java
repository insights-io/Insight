package com.meemaw.auth.signup.model;

import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.shared.model.CanExpire;
import java.net.URL;
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
  PhoneNumber phoneNumber;
  URL referrer;
  OffsetDateTime createdAt;

  public Optional<String> getReferrerCallbackUrl() {
    return Optional.ofNullable(referrer)
        .map(r -> String.join("/", r.toString(), "signup-completed-callback"));
  }

  @Override
  public int getDaysValidity() {
    return 1;
  }
}

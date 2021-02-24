package com.rebrowse.auth.signup.model;

import com.rebrowse.auth.user.model.PhoneNumber;
import com.rebrowse.shared.model.CanExpire;
import java.net.URL;
import java.time.OffsetDateTime;
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
  URL redirect;
  OffsetDateTime createdAt;

  @Override
  public int getDaysValidity() {
    return 1;
  }
}

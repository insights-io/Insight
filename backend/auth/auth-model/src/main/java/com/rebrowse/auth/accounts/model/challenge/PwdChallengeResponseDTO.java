package com.rebrowse.auth.accounts.model.challenge;

import java.net.URI;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PwdChallengeResponseDTO {

  URI redirect;
  String email;
}

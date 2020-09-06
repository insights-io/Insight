package com.meemaw.auth.sso.tfa.totp.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TfaTotpSetupStartDTO {

  String qrImage;
}

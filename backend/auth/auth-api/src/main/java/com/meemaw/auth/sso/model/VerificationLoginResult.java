package com.meemaw.auth.sso.model;

import com.meemaw.auth.sso.model.dto.VerificationResponseDTO;
import javax.ws.rs.core.NewCookie;
import lombok.Value;

@Value
public class VerificationLoginResult implements LoginResult<VerificationResponseDTO> {

  String verificationId;

  @Override
  public VerificationResponseDTO getData() {
    return new VerificationResponseDTO(verificationId);
  }

  @Override
  public NewCookie cookie(String cookieDomain) {
    return SsoVerification.cookie(verificationId, cookieDomain);
  }
}

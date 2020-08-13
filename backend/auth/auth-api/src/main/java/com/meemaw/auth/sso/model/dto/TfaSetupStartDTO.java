package com.meemaw.auth.sso.model.dto;

import lombok.Value;

@Value
public class TfaSetupStartDTO {

  String qrImageUrl;
}

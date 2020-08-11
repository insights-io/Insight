package com.meemaw.auth.verification.model;

import lombok.Value;

@Value
public class TfaSetupStart {

  String qrImageUrl;
}

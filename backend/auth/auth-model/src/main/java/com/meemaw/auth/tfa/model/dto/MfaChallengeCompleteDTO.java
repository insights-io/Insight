package com.meemaw.auth.tfa.model.dto;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MfaChallengeCompleteDTO {

  @NotNull(message = "Required")
  Integer code;
}

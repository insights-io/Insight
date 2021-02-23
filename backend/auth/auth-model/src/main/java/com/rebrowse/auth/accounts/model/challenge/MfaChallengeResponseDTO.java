package com.rebrowse.auth.accounts.model.challenge;

import com.rebrowse.auth.mfa.MfaMethod;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MfaChallengeResponseDTO {

  List<MfaMethod> methods;
}

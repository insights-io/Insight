package com.rebrowse.auth.accounts.model.challenge;

import com.rebrowse.auth.accounts.model.ChooseAccountAction;
import lombok.Value;

@Value
public class ChooseAccountPwdChallengeResponseDTO {

  ChooseAccountAction action = ChooseAccountAction.PWD_CHALLENGE;
}

package com.rebrowse.model.user;

import com.rebrowse.model.ApiRequestParams;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserUpdateParams implements ApiRequestParams {

  String fullName;
  UserRole role;
}

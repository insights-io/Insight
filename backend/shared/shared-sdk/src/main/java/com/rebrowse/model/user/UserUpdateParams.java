package com.rebrowse.model.user;

import lombok.Builder;
import lombok.Value;

import com.rebrowse.model.ApiRequestParams;

@Value
@Builder
public class UserUpdateParams implements ApiRequestParams {

  String fullName;
  UserRole role;
}

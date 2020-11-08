package com.rebrowse.model.organization;

import lombok.Builder;
import lombok.Value;

import com.rebrowse.model.ApiRequestParams;

@Value
@Builder
public class AvatarSetupUpdateParams implements ApiRequestParams {

  AvatarType type;
  String image;
}

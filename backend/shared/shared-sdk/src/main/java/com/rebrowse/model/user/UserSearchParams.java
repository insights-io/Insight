package com.rebrowse.model.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rebrowse.model.ApiRequestParams;
import com.rebrowse.api.query.QueryParam;
import com.rebrowse.model.RhsColonQueryParamSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserSearchParams implements ApiRequestParams {

  @JsonSerialize(using = RhsColonQueryParamSerializer.class)
  QueryParam<String> email;
}

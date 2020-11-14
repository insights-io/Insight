package com.rebrowse.model.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rebrowse.model.ApiRequestParams;
import com.rebrowse.model.SearchParam;
import com.rebrowse.model.SearchParamSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserSearchParams implements ApiRequestParams {

  @JsonSerialize(using = SearchParamSerializer.class)
  SearchParam<String> email;
}

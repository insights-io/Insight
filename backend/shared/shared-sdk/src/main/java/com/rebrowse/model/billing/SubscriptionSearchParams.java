package com.rebrowse.model.billing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rebrowse.api.query.SortParam;
import com.rebrowse.model.ApiRequestParams;
import com.rebrowse.model.SortParamSerializer;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SubscriptionSearchParams implements ApiRequestParams {

  @JsonSerialize(using = SortParamSerializer.class)
  SortParam sortBy;
}

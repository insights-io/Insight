package com.meemaw.shared.rest.response;

import com.rebrowse.api.RebrowseApiDataResponse;

public class CountDataResponse extends RebrowseApiDataResponse<CountDataResponse.Count> {

  static class Count {
    int count;
  }
}

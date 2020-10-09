package com.rebrowse.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class RebrowseOkDataResponse<T> {

  T data;
}

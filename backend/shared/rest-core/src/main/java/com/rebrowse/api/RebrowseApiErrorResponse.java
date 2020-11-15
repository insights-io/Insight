package com.rebrowse.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class RebrowseApiErrorResponse<T> {

  RebrowseApiError<T> error;
}

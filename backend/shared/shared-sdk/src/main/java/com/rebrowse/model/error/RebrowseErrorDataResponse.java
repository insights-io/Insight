package com.rebrowse.model.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class RebrowseErrorDataResponse<T> {

  RebrowseError<T> error;
}

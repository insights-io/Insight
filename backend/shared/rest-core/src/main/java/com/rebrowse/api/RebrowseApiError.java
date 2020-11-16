package com.rebrowse.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class RebrowseApiError<T> {

  int statusCode;
  String reason;
  String message;
  T errors;
}

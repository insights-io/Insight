package com.meemaw.shared.rest.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor
public abstract class OkDataResponse<T> {

  protected final T data;

  public static class BooleanDataResponse extends OkDataResponse<Boolean> {}
}

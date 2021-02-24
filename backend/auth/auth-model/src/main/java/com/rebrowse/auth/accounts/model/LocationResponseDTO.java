package com.rebrowse.auth.accounts.model;

import java.net.URI;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public abstract class LocationResponseDTO<T> {

  URI location;
  T action;
}

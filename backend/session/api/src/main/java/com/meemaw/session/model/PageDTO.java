package com.meemaw.session.model;

import java.util.UUID;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PageDTO {

  @NotNull(message = "may not be null")
  String orgId;

  UUID uid;

  @NotNull(message = "may not be null")
  String url;

  @NotNull(message = "may not be null")
  String referrer;

  @NotNull(message = "may not be null")
  String doctype;

  @NotNull(message = "may not be null")
  @Min(message = "must be non negative", value = 0)
  int screenWidth;

  @NotNull(message = "may not be null")
  @Min(message = "must be non negative", value = 0)
  int screenHeight;

  @NotNull(message = "may not be null")
  @Min(message = "must be non negative", value = 0)
  int width;

  @NotNull(message = "may not be null")
  @Min(message = "must be non negative", value = 0)
  int height;

  @NotNull(message = "may not be null")
  @Min(message = "must be non negative", value = 0)
  long compiledTs;

}

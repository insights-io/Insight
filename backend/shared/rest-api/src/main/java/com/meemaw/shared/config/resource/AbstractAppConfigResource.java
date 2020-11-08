package com.meemaw.shared.config.resource;

import com.meemaw.shared.config.model.AppConfigBase;

import javax.ws.rs.GET;

public abstract class AbstractAppConfigResource<T extends AppConfigBase> {

  public static final String PATH = "/v1/config";

  @GET
  public abstract T getAppConfig();
}

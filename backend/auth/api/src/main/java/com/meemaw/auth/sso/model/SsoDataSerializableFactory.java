package com.meemaw.auth.sso.model;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class SsoDataSerializableFactory implements DataSerializableFactory {

  public static final int ID = 20000;
  public static final int SSO_USER_CLASS_ID = 1;

  @Override
  public IdentifiedDataSerializable create(int typeId) {
    switch (typeId) {
      case SSO_USER_CLASS_ID:
        return new SsoUser();
      default:
        return null;
    }

  }
}

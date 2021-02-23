package com.rebrowse.auth.sso.session.model;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.rebrowse.auth.accounts.model.challenge.AuthorizationChallenge;

public class SsoDataSerializableFactory implements DataSerializableFactory {

  /* Factory */
  public static final int ID = 20000;

  /* Classes */
  public static final int SSO_USER_CLASS_ID = 1;
  public static final int CHALLENGE_ID = 2;

  @Override
  public IdentifiedDataSerializable create(int typeId) {
    if (typeId == SSO_USER_CLASS_ID) {
      return new SsoUser();
    } else if (typeId == CHALLENGE_ID) {
      return new AuthorizationChallenge();
    }
    return null;
  }
}

package com.rebrowse.auth.accounts.model.challenge;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.rebrowse.auth.sso.session.model.SsoDataSerializableFactory;
import java.io.IOException;
import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationChallenge implements IdentifiedDataSerializable {

  AuthorizationChallengeType type;
  String value;
  URI redirect;

  public static AuthorizationChallenge password(String value, URI redirect) {
    return new AuthorizationChallenge(AuthorizationChallengeType.PASSWORD, value, redirect);
  }

  public static AuthorizationChallenge mfa(String value, URI redirect) {
    return new AuthorizationChallenge(AuthorizationChallengeType.MFA, value, redirect);
  }

  @Override
  public int getFactoryId() {
    return SsoDataSerializableFactory.ID;
  }

  @Override
  public int getClassId() {
    return SsoDataSerializableFactory.CHALLENGE_ID;
  }

  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeUTF(value);
    out.writeObject(type);
    out.writeObject(redirect);
  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    this.value = in.readUTF();
    this.type = in.readObject();
    this.redirect = in.readObject();
  }
}

package com.meemaw.auth.sso.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.auth.user.model.UserRole;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SsoUser implements AuthUser, IdentifiedDataSerializable {

  UUID id;
  String email;
  UserRole role;
  String org;
  String fullName;
  OffsetDateTime createdAt;

  /**
   * Create a SsoUser from an existing AuthUser.
   *
   * @param user AuthUser
   */
  public SsoUser(AuthUser user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.role = user.getRole();
    this.org = user.getOrg();
    this.fullName = user.getFullName();
    this.createdAt = user.getCreatedAt();
  }

  @Override
  public int getFactoryId() {
    return SsoDataSerializableFactory.ID;
  }

  @Override
  public int getClassId() {
    return SsoDataSerializableFactory.SSO_USER_CLASS_ID;
  }

  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeUTF(this.id.toString());
    out.writeUTF(this.email);
    out.writeUTF(this.role.toString());
    out.writeUTF(this.org);
    out.writeUTF(this.fullName);
    out.writeObject(this.createdAt);
  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    this.id = UUID.fromString(in.readUTF());
    this.email = in.readUTF();
    this.role = UserRole.valueOf(in.readUTF());
    this.org = in.readUTF();
    this.fullName = in.readUTF();
    this.createdAt = in.readObject();
  }

  public AuthUser dto() {
    return new UserDTO(id, email, fullName, role, org, createdAt);
  }

  @Override
  public String getOrg() {
    return org;
  }
}

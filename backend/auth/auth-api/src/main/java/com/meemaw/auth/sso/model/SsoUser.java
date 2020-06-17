package com.meemaw.auth.sso.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserDTO;
import com.meemaw.auth.user.model.UserRole;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SsoUser implements AuthUser, IdentifiedDataSerializable {

  UUID id;
  String email;
  UserRole role;
  String organizationId;
  String fullName;
  Instant createdAt;

  /**
   * Create a SsoUser from an existing AuthUser.
   *
   * @param user AuthUser
   */
  public SsoUser(AuthUser user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.role = user.getRole();
    this.organizationId = user.getOrganizationId();
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
    out.writeUTF(this.organizationId);
    out.writeUTF(this.fullName);
    out.writeLong(this.createdAt.toEpochMilli());
  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    this.id = UUID.fromString(in.readUTF());
    this.email = in.readUTF();
    this.role = UserRole.valueOf(in.readUTF());
    this.organizationId = in.readUTF();
    this.fullName = in.readUTF();
    this.createdAt = Instant.ofEpochMilli(in.readLong());
  }

  public AuthUser dto() {
    return new UserDTO(id, email, fullName, role, organizationId, createdAt);
  }

  @Override
  public String getOrganizationId() {
    return organizationId;
  }
}

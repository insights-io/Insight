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
  String organizationId;
  String fullName;
  OffsetDateTime createdAt;
  OffsetDateTime updatedAt;
  String phoneNumber;
  boolean phoneNumberVerified;

  public SsoUser(AuthUser user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.role = user.getRole();
    this.organizationId = user.getOrganizationId();
    this.fullName = user.getFullName();
    this.createdAt = user.getCreatedAt();
    this.updatedAt = user.getUpdatedAt();
    this.phoneNumber = user.getPhoneNumber();
    this.phoneNumberVerified = user.isPhoneNumberVerified();
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
    out.writeObject(this.createdAt);
    out.writeObject(this.updatedAt);
    out.writeUTF(this.phoneNumber);
    out.writeBoolean(this.phoneNumberVerified);
  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    this.id = UUID.fromString(in.readUTF());
    this.email = in.readUTF();
    this.role = UserRole.valueOf(in.readUTF());
    this.organizationId = in.readUTF();
    this.fullName = in.readUTF();
    this.createdAt = in.readObject();
    this.updatedAt = in.readObject();
    this.phoneNumber = in.readUTF();
    this.phoneNumberVerified = in.readBoolean();
  }

  public AuthUser dto() {
    return new UserDTO(
        id,
        email,
        fullName,
        role,
        organizationId,
        createdAt,
        updatedAt,
        phoneNumber,
        phoneNumberVerified);
  }

  public static SsoUser as(AuthUser user) {
    return new SsoUser(user);
  }
}

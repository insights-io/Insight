package com.rebrowse.auth.sso.session.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.auth.user.model.PhoneNumber;
import com.rebrowse.auth.user.model.UserRole;
import com.rebrowse.auth.user.model.dto.PhoneNumberDTO;
import com.rebrowse.auth.user.model.dto.UserDTO;
import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SsoUser implements AuthUser, IdentifiedDataSerializable, Serializable {

  UUID id;
  String email;
  UserRole role;
  String organizationId;
  String fullName;
  OffsetDateTime createdAt;
  OffsetDateTime updatedAt;
  String phoneNumberCountryCode;
  String phoneNumberDigits;
  boolean phoneNumberVerified;

  public SsoUser(AuthUser user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.role = user.getRole();
    this.organizationId = user.getOrganizationId();
    this.fullName = user.getFullName();
    this.createdAt = user.getCreatedAt();
    this.updatedAt = user.getUpdatedAt();
    if (user.getPhoneNumber() != null) {
      this.phoneNumberCountryCode = user.getPhoneNumber().getCountryCode();
      this.phoneNumberDigits = user.getPhoneNumber().getDigits();
    }
    this.phoneNumberVerified = user.isPhoneNumberVerified();
  }

  @Override
  public PhoneNumber getPhoneNumber() {
    if (phoneNumberCountryCode == null || phoneNumberDigits == null) {
      return null;
    }
    return new PhoneNumberDTO(phoneNumberCountryCode, phoneNumberDigits);
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
        (PhoneNumberDTO) getPhoneNumber(),
        phoneNumberVerified);
  }

  public static SsoUser as(AuthUser user) {
    return new SsoUser(user);
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
    out.writeUTF(this.role.getKey());
    out.writeUTF(this.organizationId);
    out.writeUTF(this.fullName);
    out.writeObject(this.createdAt);
    out.writeObject(this.updatedAt);
    out.writeUTF(this.phoneNumberCountryCode);
    out.writeUTF(this.phoneNumberDigits);
    out.writeBoolean(this.phoneNumberVerified);
  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    this.id = UUID.fromString(in.readUTF());
    this.email = in.readUTF();
    this.role = UserRole.fromString(in.readUTF());
    this.organizationId = in.readUTF();
    this.fullName = in.readUTF();
    this.createdAt = in.readObject();
    this.updatedAt = in.readObject();
    this.phoneNumberCountryCode = in.readUTF();
    this.phoneNumberDigits = in.readUTF();
    this.phoneNumberVerified = in.readBoolean();
  }
}

package com.meemaw.auth.org.invite.model;

import com.meemaw.shared.auth.UserRole;
import java.util.UUID;

public interface CanInviteSend {

  String getEmail();

  String getOrg();

  UserRole getRole();

  UUID getCreator();

}

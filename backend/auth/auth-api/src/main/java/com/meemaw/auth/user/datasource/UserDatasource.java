package com.meemaw.auth.user.datasource;

import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.UserWithLoginInformation;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface UserDatasource {

  CompletionStage<AuthUser> createUser(
      String email,
      String fullName,
      String organizationId,
      UserRole role,
      String phoneNumber,
      SqlTransaction transaction);

  CompletionStage<AuthUser> updateUser(UUID userId, Map<String, ?> update);

  CompletionStage<Optional<AuthUser>> findUser(UUID userId);

  CompletionStage<Optional<AuthUser>> findUser(String email);

  CompletionStage<Optional<UserWithLoginInformation>> findUserWithLoginInformation(String email);

  CompletionStage<Collection<AuthUser>> findOrganizationMembers(String organizationId);
}

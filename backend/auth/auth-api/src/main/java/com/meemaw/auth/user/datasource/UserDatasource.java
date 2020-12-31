package com.meemaw.auth.user.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.PhoneNumber;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.auth.user.model.UserWithLoginInformation;
import com.meemaw.shared.rest.query.SearchDTO;
import com.meemaw.shared.rest.query.UpdateDTO;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface UserDatasource {

  CompletionStage<AuthUser> create(
      String email,
      String fullName,
      String organizationId,
      UserRole role,
      PhoneNumber phoneNumber,
      SqlTransaction transaction);

  CompletionStage<AuthUser> update(UUID userId, UpdateDTO update);

  CompletionStage<AuthUser> update(UUID userId, UpdateDTO update, SqlTransaction transaction);

  CompletionStage<Optional<AuthUser>> retrieve(UUID userId);

  CompletionStage<Optional<AuthUser>> retrieve(String email);

  CompletionStage<Optional<AuthUser>> retrieve(String email, SqlTransaction transaction);

  CompletionStage<Optional<UserWithLoginInformation>> retrieveUserWithLoginInformation(
      String email);

  CompletionStage<Collection<AuthUser>> searchOrganizationMembers(
      String organizationId, SearchDTO search);

  CompletionStage<JsonNode> count(String organizationId, SearchDTO search);
}

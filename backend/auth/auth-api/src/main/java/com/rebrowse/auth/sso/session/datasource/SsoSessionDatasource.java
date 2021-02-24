package com.rebrowse.auth.sso.session.datasource;

import com.rebrowse.auth.sso.session.model.SsoUser;
import com.rebrowse.auth.user.model.AuthUser;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface SsoSessionDatasource {

  CompletionStage<String> create(AuthUser user);

  CompletionStage<Optional<SsoUser>> retrieve(String sessionId);

  CompletionStage<Optional<SsoUser>> delete(String sessionId);

  CompletionStage<Set<String>> deleteAllForUser(UUID userId);

  CompletionStage<Set<String>> listAllForUser(UUID userId);

  CompletionStage<Set<String>> updateAllForUser(UUID userId, AuthUser user);

  CompletionStage<Void> deleteAllForOrganization(String organizationId);
}

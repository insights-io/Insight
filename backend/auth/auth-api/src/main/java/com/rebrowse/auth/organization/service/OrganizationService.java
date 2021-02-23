package com.rebrowse.auth.organization.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rebrowse.auth.organization.datasource.OrganizationDatasource;
import com.rebrowse.auth.organization.datasource.OrganizationTable;
import com.rebrowse.auth.organization.model.Organization;
import com.rebrowse.auth.organization.model.dto.AvatarSetupDTO;
import com.rebrowse.auth.sso.session.datasource.SsoSessionDatasource;
import com.rebrowse.auth.user.datasource.UserDatasource;
import com.rebrowse.auth.user.model.AuthUser;
import com.rebrowse.shared.rest.query.SearchDTO;
import com.rebrowse.shared.rest.query.UpdateDTO;
import io.vertx.core.json.JsonObject;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class OrganizationService {

  @Inject UserDatasource userDatasource;
  @Inject OrganizationDatasource organizationDatasource;
  @Inject
  SsoSessionDatasource ssoSessionDatasource;

  public CompletionStage<JsonNode> memberCount(String organizationId, SearchDTO search) {
    return userDatasource.count(organizationId, search);
  }

  @Traced
  public CompletionStage<Collection<AuthUser>> members(String organizationId, SearchDTO search) {
    return userDatasource.searchOrganizationMembers(organizationId, search);
  }

  @Traced
  public CompletionStage<Optional<Organization>> getOrganization(String organizationId) {
    return organizationDatasource.retrieve(organizationId);
  }

  @Traced
  public CompletionStage<Optional<Organization>> updateOrganization(
      String organizationId, UpdateDTO update) {
    return organizationDatasource.update(organizationId, update);
  }

  @Traced
  public CompletionStage<Optional<Organization>> setupAvatar(
      String organizationId, AvatarSetupDTO avatarSetup) {
    // TODO: upload to CDN and store link to DB
    UpdateDTO update =
        UpdateDTO.from(Map.of(OrganizationTable.AVATAR, JsonObject.mapFrom(avatarSetup)));
    return updateOrganization(organizationId, update);
  }

  // TODO: send message to other services to delete their data
  @Traced
  public CompletionStage<Boolean> delete(UUID userId, String organizationId) {
    log.debug(
        "[AUTH]: delete organization attempt user={} organization={}", userId, organizationId);

    return organizationDatasource
        .transaction()
        .thenCompose(
            transaction ->
                organizationDatasource
                    .delete(organizationId, transaction)
                    .thenCompose(
                        organizationDeleted ->
                            ssoSessionDatasource
                                .deleteAllForOrganization(organizationId)
                                .thenCompose(
                                    i1 ->
                                        transaction
                                            .commit()
                                            .thenApply(i2 -> organizationDeleted)
                                            .exceptionally(
                                                throwable -> {
                                                  log.error(
                                                      "[AUTH]: Failed to delete organization={}",
                                                      organizationId,
                                                      throwable);
                                                  transaction.rollback();
                                                  throw (RuntimeException) throwable;
                                                }))))
        .thenApply(
            deleted -> {
              log.info(
                  "[AUTH]: Successfully deleted organization={} user={}", organizationId, userId);
              return deleted;
            });
  }
}

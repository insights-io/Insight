package com.meemaw.auth.organization.service.impl;

import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.datasource.OrganizationTable;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.dto.AvatarSetupDTO;
import com.meemaw.auth.organization.service.OrganizationService;
import com.meemaw.auth.sso.session.datasource.SsoSessionDatasource;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.query.UpdateDTO;
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
import org.slf4j.MDC;

@ApplicationScoped
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

  @Inject UserDatasource userDatasource;
  @Inject OrganizationDatasource organizationDatasource;
  @Inject SsoSessionDatasource ssoSessionDatasource;

  @Override
  @Traced
  public CompletionStage<Collection<AuthUser>> members(String organizationId) {
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
    log.debug("[AUTH]: Get members for organization id={}", organizationId);
    return userDatasource.findOrganizationMembers(organizationId);
  }

  @Override
  @Traced
  public CompletionStage<Optional<Organization>> getOrganization(String organizationId) {
    MDC.put(LoggingConstants.ORGANIZATION_ID, organizationId);
    log.debug("[AUTH]: Get organization id={}", organizationId);
    return organizationDatasource.findOrganization(organizationId);
  }

  @Override
  public CompletionStage<Optional<Organization>> updateOrganization(
      String organizationId, UpdateDTO update) {
    log.debug("[AUTH]: update organization id={}", organizationId);
    return organizationDatasource.updateOrganization(organizationId, update);
  }

  @Override
  public CompletionStage<Optional<Organization>> setupAvatar(
      String organizationId, AvatarSetupDTO avatarSetup) {
    // TODO: upload to CDN and store link to DB
    UpdateDTO update =
        UpdateDTO.from(Map.of(OrganizationTable.AVATAR, JsonObject.mapFrom(avatarSetup)));
    return updateOrganization(organizationId, update);
  }

  // TODO: send message to other services to delete their data
  @Override
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
                                .exceptionally(
                                    throwable -> {
                                      transaction.rollback();
                                      throw (RuntimeException) throwable;
                                    })
                                .thenCompose(
                                    i1 ->
                                        transaction.commit().thenApply(i2 -> organizationDeleted))))
        .thenApply(
            deleted -> {
              log.info(
                  "[AUTH]: Successfully deleted organization={} user={}", organizationId, userId);
              return deleted;
            });
  }
}

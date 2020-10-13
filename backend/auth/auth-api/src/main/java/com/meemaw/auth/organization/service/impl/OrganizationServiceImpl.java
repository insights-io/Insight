package com.meemaw.auth.organization.service.impl;

import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.service.OrganizationService;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.shared.logging.LoggingConstants;
import com.meemaw.shared.rest.query.UpdateDTO;
import java.util.Collection;
import java.util.Optional;
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
}

package com.meemaw.auth.organization.service.impl;

import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.service.OrganizationService;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
public class OrganizationServiceImpl implements OrganizationService {

  @Inject UserDatasource userDatasource;
  @Inject OrganizationDatasource organizationDatasource;

  @Override
  @Traced
  public CompletionStage<Collection<AuthUser>> members(String organizationId) {
    return userDatasource.findOrganizationMembers(organizationId);
  }

  @Override
  @Traced
  public CompletionStage<Optional<Organization>> getOrganization(String organizationId) {
    return organizationDatasource.findOrganization(organizationId);
  }
}

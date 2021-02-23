package com.rebrowse.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebrowse.test.utils.auth.AbstractTestFlow;
import com.rebrowse.model.organization.Organization;
import com.rebrowse.model.organization.OrganizationUpdateParams;
import java.net.URI;

public class OrganizationFlows extends AbstractTestFlow {

  public OrganizationFlows(URI baseUri, ObjectMapper objectMapper) {
    super(baseUri, objectMapper);
  }

  public Organization update(OrganizationUpdateParams params, String sessionId) {
    return Organization.update(params, sdkRequest().sessionId(sessionId).build())
        .toCompletableFuture()
        .join();
  }

  public Organization enforceMfa(String sessionId) {
    return update(
        OrganizationUpdateParams.builder().enforceMultiFactorAuthentication(true).build(),
        sessionId);
  }
}

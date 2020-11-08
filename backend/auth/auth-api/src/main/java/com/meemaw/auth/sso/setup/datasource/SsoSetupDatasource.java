package com.meemaw.auth.sso.setup.datasource;

import com.meemaw.auth.sso.setup.model.CreateSsoSetup;
import com.meemaw.auth.sso.setup.model.dto.SsoSetup;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface SsoSetupDatasource {

  CompletionStage<SsoSetup> create(CreateSsoSetup payload);

  CompletionStage<Boolean> delete(String organizationId);

  CompletionStage<Optional<SsoSetup>> get(String organizationId);

  CompletionStage<Optional<SsoSetup>> getByDomain(String domain);
}

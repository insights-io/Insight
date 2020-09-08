package com.meemaw.auth.sso.setup.datasource;

import com.meemaw.auth.sso.setup.model.CreateSsoSetup;
import com.meemaw.auth.sso.setup.model.SsoSetupDTO;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface SsoSetupDatasource {

  CompletionStage<SsoSetupDTO> create(CreateSsoSetup payload);

  CompletionStage<Optional<SsoSetupDTO>> get(String organizationId);

  CompletionStage<Optional<SsoSetupDTO>> getByDomain(String domain);
}

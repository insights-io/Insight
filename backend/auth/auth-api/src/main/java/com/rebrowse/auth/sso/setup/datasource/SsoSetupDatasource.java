package com.rebrowse.auth.sso.setup.datasource;

import com.rebrowse.auth.sso.setup.model.CreateSsoSetup;
import com.rebrowse.auth.sso.setup.model.dto.SsoSetupDTO;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface SsoSetupDatasource {

  CompletionStage<SsoSetupDTO> create(CreateSsoSetup payload);

  CompletionStage<Boolean> delete(String organizationId);

  CompletionStage<Optional<SsoSetupDTO>> get(String organizationId);

  CompletionStage<Optional<SsoSetupDTO>> getByDomain(String domain);
}

package com.rebrowse.auth.mfa;

import com.rebrowse.auth.mfa.sms.impl.MfaSmsProvider;
import com.rebrowse.auth.mfa.totp.impl.MfaTotpProvider;
import io.quarkus.runtime.StartupEvent;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class MfaProvidersRegistry {

  @Inject
  MfaSmsProvider smsProvider;
  @Inject
  MfaTotpProvider totpProvider;

  private Map<MfaMethod, MfaProvider<?>> providerMap;

  public void init(@Observes StartupEvent event) {
    providerMap = Map.of(MfaMethod.SMS, smsProvider, MfaMethod.TOTP, totpProvider);
  }

  public MfaProvider<?> get(MfaMethod method) {
    return providerMap.get(method);
  }
}

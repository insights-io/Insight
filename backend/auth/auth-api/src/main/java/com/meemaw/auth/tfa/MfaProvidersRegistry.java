package com.meemaw.auth.tfa;

import com.meemaw.auth.tfa.sms.impl.MfaSmsProvider;
import com.meemaw.auth.tfa.totp.impl.MfaTotpProvider;
import io.quarkus.runtime.StartupEvent;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class MfaProvidersRegistry {

  @Inject MfaSmsProvider tfaSmsProvider;
  @Inject MfaTotpProvider tfaTotpProvider;

  private Map<MfaMethod, MfaProvider<?>> providerMap;

  public void init(@Observes StartupEvent event) {
    providerMap = Map.of(MfaMethod.SMS, tfaSmsProvider, MfaMethod.TOTP, tfaTotpProvider);
  }

  public MfaProvider<?> get(MfaMethod method) {
    return providerMap.get(method);
  }
}

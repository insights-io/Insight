package com.meemaw.auth.sso.tfa;

import com.meemaw.auth.sso.tfa.sms.impl.TfaSmsProvider;
import com.meemaw.auth.sso.tfa.totp.impl.TfaTotpProvider;
import io.quarkus.runtime.StartupEvent;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class TfaProvidersRegistry {

  @Inject TfaSmsProvider tfaSmsProvider;
  @Inject TfaTotpProvider tfaTotpProvider;

  private Map<TfaMethod, TfaProvider<?>> providerMap;

  public void init(@Observes StartupEvent event) {
    providerMap = Map.of(TfaMethod.SMS, tfaSmsProvider, TfaMethod.TOTP, tfaTotpProvider);
  }

  public TfaProvider<?> get(TfaMethod method) {
    return providerMap.get(method);
  }
}

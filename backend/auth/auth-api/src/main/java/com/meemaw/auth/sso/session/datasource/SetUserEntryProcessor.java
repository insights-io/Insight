package com.meemaw.auth.sso.session.datasource;

import com.hazelcast.map.EntryProcessor;
import com.meemaw.auth.sso.session.model.SsoUser;
import java.util.Map.Entry;

public class SetUserEntryProcessor implements EntryProcessor<String, SsoUser, Void> {

  private final SsoUser user;

  public SetUserEntryProcessor(SsoUser user) {
    this.user = user;
  }

  @Override
  public Void process(Entry<String, SsoUser> entry) {
    entry.setValue(user);
    return null;
  }
}

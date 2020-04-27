package com.meemaw.session.service;

import com.meemaw.session.datasource.PageDatasource;
import com.meemaw.session.model.Page;
import com.meemaw.session.model.PageIdentity;
import io.smallrye.mutiny.Uni;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class PageService {

  @Inject
  PageDatasource pageDatasource;

  public Uni<PageIdentity> process(Page page) {
    UUID pageId = UUID.randomUUID();

    UUID uid = Optional.ofNullable(page.getUid()).orElseGet(UUID::randomUUID);
    String org = page.getOrganization();

    // unrecognized device; start a new session
    if (uid != page.getUid()) {
      UUID sessionId = UUID.randomUUID();
      log.info("Generating new session {} uid {} pageId {} org {}", sessionId, uid, pageId, org);
      return pageDatasource.insertPage(pageId, uid, sessionId, page);
    }

    // recognized device; try to link it with an existing session
    return pageDatasource.findUserSessionLink(org, uid).onItem().produceUni(
        maybeSessionId -> {
          UUID sessionId = maybeSessionId.orElseGet(() -> {
            log.info("Could not link session for uid {}, pageId {} org {}", uid, pageId, org);
            return UUID.randomUUID();
          });
          return pageDatasource.insertPage(pageId, uid, sessionId, page);
        }
    );
  }

  public Uni<Integer> activePageCount() {
    return pageDatasource.activePageCount();
  }

}

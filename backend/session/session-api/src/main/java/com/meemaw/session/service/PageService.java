package com.meemaw.session.service;

import com.meemaw.session.datasource.PageDatasource;
import com.meemaw.session.model.CreatePageDTO;
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

  @Inject PageDatasource pageDatasource;

  /**
   * @param createPageDTO
   * @return
   */
  public Uni<PageIdentity> process(CreatePageDTO createPageDTO) {

    UUID pageId = UUID.randomUUID();
    UUID uid = Optional.ofNullable(createPageDTO.getUid()).orElseGet(UUID::randomUUID);
    String orgID = createPageDTO.getOrgId();

    // unrecognized device; start a new session
    if (uid != createPageDTO.getUid()) {
      UUID sessionId = UUID.randomUUID();
      log.info(
          "Generating new sessionID={} uid={} pageID={} orgID={}", sessionId, uid, pageId, orgID);
      return pageDatasource.insertPage(pageId, uid, sessionId, createPageDTO);
    }

    // recognized device; try to link it with an existing session
    return pageDatasource
        .findUserSessionLink(orgID, uid)
        .onItem()
        .produceUni(
            maybeSessionId -> {
              UUID sessionId =
                  maybeSessionId.orElseGet(
                      () -> {
                        log.info(
                            "Could not link session for uid={}, pageID={} orgID={}",
                            uid,
                            pageId,
                            orgID);
                        return UUID.randomUUID();
                      });
              return pageDatasource.insertPage(pageId, uid, sessionId, createPageDTO);
            });
  }

  public Uni<Integer> activePageCount() {
    return pageDatasource.activePageCount();
  }

  public Uni<Optional<Page>> getPage(UUID pageID, UUID sessionID, String orgID) {
    return pageDatasource.getPage(pageID, sessionID, orgID);
  }
}

package com.meemaw.model.page;

import java.util.UUID;

public class PageIdentityDTOBuilder {

    private UUID uid;
    private UUID sessionId;
    private UUID pageId;

    public PageIdentityDTOBuilder setUid(UUID uid) {
        this.uid = uid;
        return this;
    }

    public PageIdentityDTOBuilder setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public PageIdentityDTOBuilder setPageId(UUID pageId) {
        this.pageId = pageId;
        return this;
    }

    public PageIdentityDTO build() {
        return new PageIdentityDTO(uid, sessionId, pageId);
    }
}
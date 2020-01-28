package com.meemaw.model.page;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class PageIdentityDTO {

    public PageIdentityDTO(UUID uid, UUID sessionId, UUID pageId) {
        this.uid = uid;
        this.sessionId = sessionId;
        this.pageId = pageId;
    }

    @JsonProperty("uid")
    private UUID uid;

    @JsonProperty("sessionId")
    private UUID sessionId;

    @JsonProperty("pageId")
    private UUID pageId;

    public UUID getUid() {
        return uid;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getPageId() {
        return pageId;
    }
}

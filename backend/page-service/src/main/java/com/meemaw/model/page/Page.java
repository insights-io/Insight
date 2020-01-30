package com.meemaw.model.page;

import java.util.Objects;
import java.util.UUID;

public class Page {

    private final String organization;
    private final UUID uid;
    private final String doctype;
    private final String url;
    private final String referrer;
    private final int height;
    private final int width;
    private final int screenHeight;
    private final int screenWidth;
    private final int compiledTimestamp;

    private Page(PageDTO dto) {
        this.organization = dto.getOrg();
        this.uid = dto.getUid();
        this.doctype = dto.getDoctype();
        this.url = dto.getUrl();
        this.referrer = dto.getReferrer();
        this.height = dto.getHeight();
        this.width = dto.getWidth();
        this.screenHeight = dto.getScreenHeight();
        this.screenWidth = dto.getScreenWidth();
        this.compiledTimestamp = dto.getCompiledTimestamp();
    }

    public static Page from(PageDTO dto) {
        return new Page(Objects.requireNonNull(dto));
    }

    public String getOrganization() {
        return organization;
    }

    public UUID getUid() {
        return uid;
    }

    public String getDoctype() {
        return doctype;
    }

    public String getUrl() {
        return url;
    }

    public String getReferrer() {
        return referrer;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getCompiledTimestamp() {
        return compiledTimestamp;
    }
}

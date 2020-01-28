package com.meemaw.model.page;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public class PageDTO {

    @JsonProperty("org")
    @NotNull(message = "may not be null")
    private String org;

    @JsonProperty("uid")
    private UUID uid;

    @JsonProperty("url")
    @NotNull(message = "may not be null")
    private String url;

    @JsonProperty("referrer")
    @NotNull(message = "may not be null")
    private String referrer;

    @JsonProperty("doctype")
    @NotNull(message = "may not be null")
    private String doctype;

    @JsonProperty("screenWidth")
    @NotNull(message = "may not be null")
    @Min(message = "must be non negative", value = 0)
    private int screenWidth;

    @JsonProperty("screenHeight")
    @NotNull(message = "may not be null")
    @Min(message = "must be non negative", value = 0)
    private int screenHeight;

    @JsonProperty("width")
    @NotNull(message = "may not be null")
    @Min(message = "must be non negative", value = 0)
    private int width;

    @JsonProperty("height")
    @NotNull(message = "may not be null")
    @Min(message = "must be non negative", value = 0)
    private int height;

    @JsonProperty("compiledTimestamp")
    @NotNull(message = "may not be null")
    @Min(message = "must be non negative", value = 0)
    private int compiledTimestamp;

    public String getOrg() {
        return org;
    }

    public UUID getUid() {
        return uid;
    }

    public String getUrl() {
        return url;
    }

    public String getReferrer() {
        return referrer;
    }

    public String getDoctype() {
        return doctype;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCompiledTimestamp() {
        return compiledTimestamp;
    }
}

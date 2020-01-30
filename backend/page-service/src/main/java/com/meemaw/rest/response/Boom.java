package com.meemaw.rest.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class Boom<T> {

    @JsonProperty("statusCode")
    private int statusCode;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("message")
    private String message;

    @JsonProperty("errors")
    private T errors;

    public Boom(Status status) {
        this.statusCode = status.getStatusCode();
        this.reason = status.getReasonPhrase();
        this.message = status.getReasonPhrase();
    }

    public Boom<T> message(String message) {
        this.message = message;
        return this;
    }

    public Boom<T> errors(T errors) {
        this.errors = errors;
        return this;
    }


    public static <T> Boom<T> status(int statusCode) {
        return new Boom<>(Status.fromStatusCode(statusCode));
    }

    public static <T> Boom<T> status(Status status) {
        return new Boom<>(status);
    }

    public Response response() {
        return DataResponse.error(this).response(statusCode);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public T getErrors() {
        return errors;
    }

    public String getReason() {
        return reason;
    }
}

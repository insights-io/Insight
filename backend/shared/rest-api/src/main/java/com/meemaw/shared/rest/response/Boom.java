package com.meemaw.shared.rest.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.meemaw.shared.rest.exception.BoomException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

public class Boom<T> {

  @JsonProperty("statusCode")
  private int statusCode;

  @JsonProperty("reason")
  private String reason;

  @JsonProperty("message")
  private String message;

  @JsonProperty("errors")
  private T errors;

  public Boom(StatusType status) {
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

  public static <T> Boom<T> status(StatusType status) {
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

  public BoomException exception() {
    return new BoomException(this);
  }

  public static <T> Boom<T> badRequest() {
    return Boom.status(Status.BAD_REQUEST);
  }

  public static <T> Boom<T> notFound() {
    return Boom.status(Status.NOT_FOUND);
  }

  public static <T> Boom<T> serverError() {
    return Boom.status(Status.INTERNAL_SERVER_ERROR);
  }

}

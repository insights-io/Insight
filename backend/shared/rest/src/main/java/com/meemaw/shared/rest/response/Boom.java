package com.meemaw.shared.rest.response;

import com.meemaw.shared.rest.exception.BoomException;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Boom<T> {

  private final int statusCode;

  private final String reason;

  private String message;

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

  public Response.ResponseBuilder responseBuilder() {
    return DataResponse.error(this).builder(statusCode);
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

  public BoomException exception(Throwable throwable) {
    return new BoomException(throwable, this);
  }

  public static <T> Boom<T> badRequest() {
    return Boom.status(Status.BAD_REQUEST);
  }

  public static <T> Boom<T> unauthorized() {
    return Boom.status(Status.UNAUTHORIZED);
  }

  public static <T> Boom<T> validationErrors(Map<String, ?> errors) {
    return (Boom<T>) badRequest().message("Validation Error").errors(errors);
  }

  public static <T> Boom<T> notFound() {
    return Boom.status(Status.NOT_FOUND);
  }

  public static <T> Boom<T> serverError() {
    return Boom.status(Status.INTERNAL_SERVER_ERROR);
  }
}

package com.rebrowse.shared.rest.response;

import com.rebrowse.api.RebrowseApiDataResponse;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class DataResponse<T> extends RebrowseApiDataResponse<T> {

  Boom<?> error;

  public DataResponse(T data, Boom<?> boom) {
    super(data);
    this.error = boom;
  }

  public static <T> DataResponse<T> data(T data) {
    return new DataResponse<>(Objects.requireNonNull(data), null);
  }

  public static <T> Response ok(T data) {
    return DataResponse.okBuilder(data).build();
  }

  public static <T> Response created(T data) {
    return DataResponse.data(data).builder(201).build();
  }

  public static Response noContent() {
    return Response.noContent().build();
  }

  public static <T> Response.ResponseBuilder okBuilder(T data) {
    return DataResponse.data(data).builder(200);
  }

  public static <T> DataResponse<T> error(Boom<?> error) {
    return new DataResponse<>(null, Objects.requireNonNull(error));
  }

  public Response.ResponseBuilder builder(int statusCode) {
    return Response.status(statusCode).entity(this).type(MediaType.APPLICATION_JSON);
  }

  public Response.ResponseBuilder builder() {
    return Response.status(Objects.requireNonNull(error).getStatusCode())
        .entity(this)
        .type(MediaType.APPLICATION_JSON);
  }

  public Response response(int statusCode) {
    return builder(statusCode).build();
  }

  public Response response(Response.Status status) {
    return response(status.getStatusCode());
  }
}

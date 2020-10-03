package com.meemaw.shared.rest.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ErrorDataResponse<T extends Boom<?>> {

  T boom;

  public static final String NOT_FOUND_EXAMPLE =
      "{\n"
          + "  \"error\": {\n"
          + "    \"statusCode\": 404,\n"
          + "    \"reason\": \"Not Found\",\n"
          + "    \"message\": \"Not Found\"\n"
          + "  }\n"
          + "}";

  public static final String BAD_REQUEST_EXAMPLE =
      "{\n"
          + "  \"error\": {\n"
          + "    \"statusCode\": 404,\n"
          + "    \"reason\": \"Bad Request\",\n"
          + "    \"message\": \"Bad Request\",\n"
          + "    \"errors\": {\n"
          + "      \"body\": \"Required\"\n"
          + "    }\n"
          + "  }\n"
          + "}";

  public static final String UNAUTHORIZED_EXAMPLE =
      "{\n"
          + "  \"error\": {\n"
          + "    \"statusCode\": 401,\n"
          + "    \"reason\": \"Unauthorized\",\n"
          + "    \"message\": \"Unauthorized\"\n"
          + "  }\n"
          + "}";

  public static final String SERVER_ERROR_EXAMPLE =
      "{\n"
          + "  \"error\": {\n"
          + "    \"statusCode\": 500,\n"
          + "    \"reason\": \"Internal Server Error\",\n"
          + "    \"message\": \"Internal Server Error\"\n"
          + "  }\n"
          + "}";
}

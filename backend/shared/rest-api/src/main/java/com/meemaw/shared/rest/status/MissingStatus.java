package com.meemaw.shared.rest.status;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import lombok.Getter;

/**
 * Status codes not part of the JAX-RS {@link Status} enum.
 */
@Getter
public enum MissingStatus implements StatusType {

  /**
   * 422 Unprocessable Entity, see
   * <a href="https://tools.ietf.org/html/rfc4918#section-11.2">https://tools.ietf.org/html/rfc4918#section-11.2</a>
   */
  UNPROCESSABLE_ENTITY(422, "Unprocessable Entity");

  MissingStatus(int code, String reasonPhrase) {
    this.statusCode = code;
    this.reasonPhrase = reasonPhrase;
    this.family = Family.familyOf(code);
  }

  private int statusCode;
  private String reasonPhrase;
  private Family family;

}

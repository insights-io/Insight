package com.meemaw.shared.rest.exception;

import com.meemaw.shared.rest.response.Boom;

public class BoomException extends RuntimeException {

  public BoomException(Boom boom) {
    super();
    this.boom = boom;
  }

  private Boom boom;

  public Boom getBoom() {
    return boom;
  }
}

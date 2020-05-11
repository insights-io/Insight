package com.meemaw.shared.rest.exception;

import com.meemaw.shared.rest.response.Boom;

public class BoomException extends RuntimeException {

  private final Boom<?> boom;

  public BoomException(Boom boom) {
    super();
    this.boom = boom;
  }

  public BoomException(Throwable throwable, Boom boom) {
    super(throwable);
    this.boom = boom;
  }

  public Boom<?> getBoom() {
    return boom;
  }
}

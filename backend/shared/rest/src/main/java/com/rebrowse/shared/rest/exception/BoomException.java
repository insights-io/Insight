package com.rebrowse.shared.rest.exception;

import com.rebrowse.shared.rest.response.Boom;

public class BoomException extends RuntimeException {

  private final Boom<?> boom;

  public BoomException(Boom<?> boom) {
    super(boom.getMessage());
    this.boom = boom;
  }

  public BoomException(Throwable throwable, Boom<?> boom) {
    super(throwable);
    this.boom = boom;
  }

  public Boom<?> getBoom() {
    return boom;
  }
}

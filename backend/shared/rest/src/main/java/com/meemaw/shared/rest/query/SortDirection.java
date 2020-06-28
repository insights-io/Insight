package com.meemaw.shared.rest.query;

import lombok.ToString;

@ToString
public enum SortDirection {
  ASC {
    @Override
    public char getSymbol() {
      return '+';
    }
  },
  DESC {
    @Override
    public char getSymbol() {
      return '-';
    }
  };

  public abstract char getSymbol();
}

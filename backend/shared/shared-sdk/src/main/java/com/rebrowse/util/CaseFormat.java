package com.rebrowse.util;

public enum CaseFormat {
  SNAKE_CASE {
    @Override
    public boolean isDelimiter(char c) {
      return c == '_';
    }

    @Override
    public void transform(StringBuilder builder, int index) {
      builder.replace(
          index, index + 1, String.valueOf(Character.toLowerCase(builder.charAt(index))));
      builder.insert(index, '_');
    }
  },
  CAMEL_CASE {
    @Override
    public boolean isDelimiter(char c) {
      return Character.isUpperCase(c);
    }

    @Override
    public void transform(StringBuilder builder, int index) {
      builder.deleteCharAt(index);
      builder.replace(
          index, index + 1, String.valueOf(Character.toUpperCase(builder.charAt(index))));
    }
  };

  public String to(CaseFormat caseFormat, String value) {
    if (value == null || value.isBlank()) {
      return value;
    }
    StringBuilder builder = new StringBuilder(value);
    for (int i = 0; i < builder.length(); i++) {
      if (isDelimiter(builder.charAt(i))) {
        caseFormat.transform(builder, i);
      }
    }
    return builder.toString();
  }

  public abstract boolean isDelimiter(char c);

  public abstract void transform(StringBuilder builder, int index);
}

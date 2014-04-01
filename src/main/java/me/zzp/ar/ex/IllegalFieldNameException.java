package me.zzp.ar.ex;

public class IllegalFieldNameException extends RuntimeException {
  public IllegalFieldNameException(String fieldName) {
    super(fieldName);
  }
}

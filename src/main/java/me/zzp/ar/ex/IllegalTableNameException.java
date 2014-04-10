package me.zzp.ar.ex;

public class IllegalTableNameException extends RuntimeException {
  public IllegalTableNameException(String tableName, Throwable e) {
    super(String.format("illegal table %s", tableName), e);
  }
}

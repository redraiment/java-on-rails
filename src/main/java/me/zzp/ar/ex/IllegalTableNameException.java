package me.zzp.ar.ex;

public class IllegalTableNameException extends RuntimeException {
  public IllegalTableNameException(String tableName, Throwable e) {
    super(tableName, e);
  }
}

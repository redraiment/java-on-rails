package me.zzp.ar.ex;

public class SqlExecuteException extends RuntimeException {
  public SqlExecuteException(String sql, Throwable cause) {
    super(sql, cause);
  }
}

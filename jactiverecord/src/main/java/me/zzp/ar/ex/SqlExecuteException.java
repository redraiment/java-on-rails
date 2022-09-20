package me.zzp.ar.ex;

/**
 * 执行SQL时遇到任何问题抛出此异常。
 * 
 * @since 1.0
 * @author redraiment
 */
public class SqlExecuteException extends RuntimeException {
  public SqlExecuteException(String sql, Throwable cause) {
    super(sql, cause);
  }
}

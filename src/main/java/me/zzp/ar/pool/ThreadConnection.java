package me.zzp.ar.pool;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import me.zzp.ar.ex.DBOpenException;

/**
 * 在多线程环境下，为每个线程提供独立的数据库连接。
 * 
 * @since 2.0
 * @author redraiment
 */
public final class ThreadConnection extends ThreadLocal<Connection> {
  private final DataSource pool;  

  /**
   * 提供数据库源。
   * 
   * @param pool 数据库连接池。
   */
  public ThreadConnection(DataSource pool) {
    this.pool = pool;
  }

  /**
   * 初始连接。
   * 
   * @return 返回新的可用连接。
   */
  @Override
  protected Connection initialValue() {
    try {
      return pool.getConnection();
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }
}

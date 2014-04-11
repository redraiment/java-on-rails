package me.zzp.ar.pool;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import me.zzp.ar.ex.DBOpenException;

public final class ThreadConnection extends ThreadLocal<Connection> {
  private final DataSource pool;  

  public ThreadConnection(DataSource pool) {
    this.pool = pool;
  }

  @Override
  protected Connection initialValue() {
    try {
      return pool.getConnection();
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }
}

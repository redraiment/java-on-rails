package me.zzp.ar.pool;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import me.zzp.ar.DB;
import me.zzp.ar.ex.DBOpenException;

public class MemoryDataSource implements DataSource {
  private final Connection c;

  public MemoryDataSource(Connection c) {
    this.c = new PooledConnection(c);
  }
  
  public static DB open(String url) {
    try {
      return DB.open(new MemoryDataSource(DriverManager.getConnection(url)));
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }
  
  @Override
  public Connection getConnection() throws SQLException {
    return c;
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return c;
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}

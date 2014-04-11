package me.zzp.ar.pool;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Simple DataSource, returns a new DB connection always.
 * @author redraiment
 */
public final class JdbcDataSource implements DataSource {
  private final String url;
  private final Properties info;

  public JdbcDataSource(String url, Properties info) {
    this.url = url;
    this.info = info;
  }

  /**
   * Returns new connection always.
   * @return a new connection.
   * @throws SQLException 
   */
  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, info);
  }

  /**
   * Returns new connection always.
   * @return a new connection.
   * @throws SQLException 
   */
  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  /**
   * Not supported yet.
   * @return nothing
   * @throws SQLException 
   */
  @Override
  public PrintWriter getLogWriter() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Not supported yet.
   * @throws SQLException 
   */
  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Not supported yet.
   * @throws SQLException 
   */
  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Not supported yet.
   * @return nothing
   * @throws SQLException 
   */
  @Override
  public int getLoginTimeout() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Not supported yet.
   * @return nothing
   * @throws SQLFeatureNotSupportedException
   */
  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Not supported yet.
   * @return nothing
   * @throws SQLException 
   */
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Not supported yet.
   * @return nothing
   * @throws SQLException 
   */
  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}

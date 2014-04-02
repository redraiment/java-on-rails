package me.zzp.ar.d;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import me.zzp.ar.ex.DBOpenException;

public class PostgreSQLDialect implements Dialect {
  @Override
  public boolean accept(Connection c) {
    try {
      DatabaseMetaData d = c.getMetaData();
      String name = d.getDatabaseProductName(); // PostgreSQL
      return name.toLowerCase().contains("postgresql");
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }

  @Override
  public String getIdentity() {
    return "serial primary key";
  }
}

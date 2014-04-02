package me.zzp.ar.d;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import me.zzp.ar.ex.DBOpenException;

public class HyperSQLDialect implements Dialect {
  @Override
  public boolean accept(Connection c) {
    try {
      DatabaseMetaData d = c.getMetaData();
      String name = d.getDatabaseProductName(); // HSQL Database Engine
      return name.toLowerCase().contains("hsql");
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }

  @Override
  public String getIdentity() {
    return "integer primary key generated always as identity (start with 1, increment by 1)";
  }
}

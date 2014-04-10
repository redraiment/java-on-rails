package me.zzp.ar.d;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import me.zzp.ar.ex.DBOpenException;

public final class DerbyDialect implements Dialect {
  @Override
  public boolean accept(Connection c) {
    try {
      DatabaseMetaData d = c.getMetaData();
      String name = d.getDatabaseProductName(); // Apache Derby
      return name.toLowerCase().contains("derby");
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }

  @Override
  public String getIdentity() {
    return "integer primary key generated always as identity (start with 1, increment by 1)";
  }

  @Override
  public String getCaseIdentifier(String identifier) {
    return identifier.toUpperCase();
  }
}

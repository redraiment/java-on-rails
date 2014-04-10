package me.zzp.ar.d;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import me.zzp.ar.ex.DBOpenException;

public class SybaseDialect implements Dialect {
  @Override
  public boolean accept(Connection c) {
    try {
      DatabaseMetaData d = c.getMetaData();
      String name = d.getDatabaseProductName(); // Adaptive Server Enterprise
      return name.equalsIgnoreCase("Adaptive Server Enterprise");
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }

  @Override
  public String getIdentity() {
    return "integer identity primary key";
  }

  @Override
  public String getCaseIdentifier(String identifier) {
    return identifier.toLowerCase();
  }
}

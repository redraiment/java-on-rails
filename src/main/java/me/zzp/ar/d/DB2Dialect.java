package me.zzp.ar.d;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import me.zzp.ar.ex.DBOpenException;

public class DB2Dialect implements Dialect {
  @Override
  public boolean accept(Connection c) {
    try {
      DatabaseMetaData d = c.getMetaData();
      String name = d.getDatabaseProductName(); // DB2/LINUXX8664
      return name.toLowerCase().contains("db2");
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }

  @Override
  public String getIdentity() {
    return "integer primary key generated by default as identity";
  }
}
package me.zzp.ar.d;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import me.zzp.ar.ex.DBOpenException;

public class MySQLDialect implements Dialect {
  @Override
  public boolean accept(Connection c) {
    try {
      DatabaseMetaData d = c.getMetaData();
      String name = d.getDatabaseProductName(); // MySQL
      return name.toLowerCase().contains("mysql");
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }

  @Override
  public String getIdentity() {
    return "integer primary key auto_increment";
  }
}

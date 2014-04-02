package me.zzp.ar.d;

import java.sql.Connection;

public interface Dialect {
  public boolean accept(Connection c);
  public String getIdentity();
}

package me.zzp.ar;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import me.zzp.ar.ex.DBOpenException;
import me.zzp.ar.ex.IllegalTableNameException;
import me.zzp.ar.ex.SqlExecuteException;
import me.zzp.util.Seq;

public final class DB {

  public static DB open(String url) {
    try {
      return new DB(DriverManager.getConnection(url));
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }

  public static DB open(String url, Properties info) {
    try {
      return new DB(DriverManager.getConnection(url, info));
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }

  public static DB open(String url, String username, String password) {
    try {
      return new DB(DriverManager.getConnection(url, username, password));
    } catch (SQLException e) {
      throw new DBOpenException(e);
    }
  }

  private final Connection base;
  private final Map<String, Map<String, Integer>> columns;
  private final Map<String, Map<String, Association>> relations;

  private DB(Connection base) {
    this.base = base;
    this.columns = new HashMap<String, Map<String, Integer>>();
    this.relations = new HashMap<String, Map<String, Association>>();
  }

  private Map<String, Integer> getColumns(String name) throws SQLException {
    if (!columns.containsKey(name)) {
      synchronized (columns) {
        if (!columns.containsKey(name)) {
          String catalog, schema, table;
          String[] patterns = name.split("\\.");
          if (patterns.length == 1) {
            catalog = null;
            schema = null;
            table = patterns[0];
          } else if (patterns.length == 2) {
            catalog = null;
            schema = patterns[0];
            table = patterns[1];
          } else if (patterns.length == 3) {
            catalog = patterns[0];
            schema = patterns[1];
            table = patterns[2];
          } else {
            throw new IllegalArgumentException(String.format("Illegal table name: %s", name));
          }

          Map<String, Integer> column = new LinkedHashMap<String, Integer>();
          DatabaseMetaData db = base.getMetaData();
          ResultSet rs = db.getColumns(catalog, schema, table, null);
          while (rs.next()) {
            String columnName = rs.getString("column_name");
            if (columnName.equalsIgnoreCase("id")
                || columnName.equalsIgnoreCase("created_at")
                || columnName.equalsIgnoreCase("updated_at")) {
              continue;
            }
            column.put(columnName.toLowerCase(), rs.getInt("data_type"));
          }
          columns.put(name, column);
        }
      }
    }
    return columns.get(name);
  }

  public Table active(String name) {
    name = name.toLowerCase();

    if (!relations.containsKey(name)) {
      synchronized (relations) {
        if (!relations.containsKey(name)) {
          relations.put(name, new HashMap<String, Association>());
        }
      }
    }

    try {
      return new Table(this, name, getColumns(name), relations.get(name));
    } catch (SQLException e) {
      throw new IllegalTableNameException(name, e);
    }
  }

  public PreparedStatement prepare(String sql, Object[] params, int[] types) {
    try {
      PreparedStatement call = base.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      if (params != null && params.length > 0) {
        for (int i = 0; i < params.length; i++) {
          if (params[i] != null) {
            call.setObject(i + 1, params[i]);
          } else {
            call.setNull(i + 1, types[i]);
          }
        }
      }
      return call;
    } catch (SQLException e) {
      throw new SqlExecuteException(sql, e);
    }
  }

  public void execute(String sql, Object[] params, int[] types) {
    try {
      PreparedStatement call = prepare(sql, params, types);
      call.executeUpdate();
      call.close();
    } catch (SQLException e) {
      throw new SqlExecuteException(sql, e);
    }
  }

  public void execute(String sql) {
    execute(sql, null, null);
  }

  public ResultSet query(String sql, Object... params) {
    try {
      PreparedStatement call = prepare(sql, params, null);
      return call.executeQuery();
    } catch (SQLException e) {
      throw new SqlExecuteException(sql, e);
    }
  }

  public Table createTable(String name, String... columns) {
    String template = "create table %s (id integer primary key autoincrement, %s, created_at timestamp, updated_at timestamp)";
    execute(String.format(template, name, Seq.join(Arrays.asList(columns), ",")));
    return active(name);
  }

  public void dropTable(String name) {
    execute(String.format("drop table %s", name));
  }

  public void createIndex(String name, String table, String... columns) {
    execute(String.format("create index %s on %s(%s)", name, table, Seq.join(Arrays.asList(columns), ", ")));
  }

  public void dropIndex(String name, String table) {
    execute(String.format("drop index %s on %s", name, table));
  }

  public void close() {
    try {
      base.close();
    } catch (SQLException e) {
      throw new RuntimeException("close connection fail", e);
    }
  }

  /* Utility */
  public static Timestamp now() {
    return new Timestamp(System.currentTimeMillis());
  }
}

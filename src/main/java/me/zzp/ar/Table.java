package me.zzp.ar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import me.zzp.ar.ex.IllegalFieldNameException;
import me.zzp.ar.ex.SqlExecuteException;
import me.zzp.ar.sql.AbstractSqlBuilder;
import me.zzp.ar.sql.SqlBuilder;
import me.zzp.ar.sql.TSqlBuilder;
import me.zzp.util.Seq;

public final class Table {
  final DB dbo;
  final String name;
  final Map<String, Integer> columns;
  final Map<String, Association> relations;
  final String primaryKey;

  private String foreignTable;
  private final Map<String, Integer> foreignKeys = new HashMap<String, Integer>();

  Table(DB dbo, String name, Map<String, Integer> columns, Map<String, Association> relations) {
    this.dbo = dbo;
    this.name = name;
    this.columns = columns;
    this.relations = relations;
    this.primaryKey = name.concat(".id");
  }

  /* Association */
  private Association assoc(String name, boolean onlyOne, boolean ancestor) {
    Association assoc = new Association(relations, name, onlyOne, ancestor);
    relations.put(name.toLowerCase(), assoc);
    return assoc;
  }

  public Association belongsTo(String name) {
    return assoc(name, true, false);
  }

  public Association hasOne(String name) {
    return assoc(name, true, true);
  }

  public Association hasMany(String name) {
    return assoc(name, false, true);
  }

  public Association hasAndBelongsToMany(String name) {
    return assoc(name, false, false);
  }

  private String[] getForeignKeys() {
    List<String> conditions = new ArrayList<String>();
    for (Map.Entry<String, Integer> e : foreignKeys.entrySet()) {
      conditions.add(String.format("%s.%s = %d", name, e.getKey(), e.getValue()));
    }
    return conditions.toArray(new String[0]);
  }

  public Table constrain(String key, int id) {
    foreignKeys.put(key.toLowerCase(), id);
    return this;
  }

  public Table join(String table) {
    this.foreignTable = table;
    return this;
  }

  /* CRUD */
  public Record create(Object... args) {
    Map<String, Object> data = new HashMap<String, Object>();
    data.putAll(foreignKeys);
    for (int i = 0; i < args.length; i += 2) {
      String key = args[i].toString().toLowerCase();
      if (key.endsWith(":")) {
        key = key.substring(0, key.length() - 1);
      }
      if (!columns.containsKey(key)) {
        throw new IllegalFieldNameException(key);
      }
      Object value = args[i + 1];
      data.put(key, value);
    }

    String[] fields = new String[data.size() + 2];
    int[] types = new int[data.size() + 2];
    Object[] values = new Object[data.size() + 2];
    int index = 0;
    for (Map.Entry<String, Object> e : data.entrySet()) {
      fields[index] = e.getKey();
      types[index] = columns.get(e.getKey());
      values[index] = e.getValue();
      index++;
    }
    Seq.assignAt(fields, Seq.array(-2, -1), "created_at", "updated_at");
    Seq.assignAt(types, Seq.array(-2, -1), Types.TIMESTAMP, Types.TIMESTAMP);
    Seq.assignAt(values, Seq.array(-2, -1), DB.now(), DB.now());

    SqlBuilder sql = new TSqlBuilder();
    sql.insert().into(name).values(fields);
    PreparedStatement call = dbo.prepare(sql.toString(), values, types);
    try {
      int id = 0;
      if (call.executeUpdate() > 0) {
        ResultSet rs = call.getGeneratedKeys();
        if (rs != null) {
          id = rs.getInt(1);
          rs.close();
        }
      }
      call.close();
      return id > 0 ? find(id) : null;
    } catch (SQLException e) {
      throw new SqlExecuteException(sql.toString(), e);
    }
  }

  public void update(Record record) {
    String[] fields = new String[columns.size() + 1];
    int[] types = new int[columns.size() + 1];
    Object[] values = new Object[columns.size() + 1];
    int index = 0;
    for (String column : columns.keySet()) {
      fields[index] = column;
      types[index] = columns.get(column);
      values[index] = record.get(column);
      index++;
    }

    fields[columns.size()] = "updated_at";
    types[columns.size()] = Types.TIMESTAMP;
    values[columns.size()] = DB.now();

    SqlBuilder sql = new TSqlBuilder();
    sql.update(name).set(fields).where(String.format("%s = %d", primaryKey, record.getInt("id")));
    dbo.execute(sql.toString(), values, types);
  }

  public void delete(Record record) {
    int id = record.get("id");
    SqlBuilder sql = new TSqlBuilder();
    sql.delete().from(name).where(String.format("%s = %d", primaryKey, id));
    dbo.execute(sql.toString());
  }

  public void purge() {
    // TODO: need enhancement
    for (Record record : all()) {
      delete(record);
    }
  }

  public List<Record> query(String sql, Object... args) {
    List<Record> records = new LinkedList<Record>();
    ResultSet rs = dbo.query(sql, args);
    try {
      ResultSetMetaData meta = rs.getMetaData();
      while (rs.next()) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
          String label = meta.getColumnLabel(i).toLowerCase();
          values.put(label, rs.getObject(label));
        }
        records.add(new Record(this, values));
      }
      rs.close();
      rs.getStatement().close();
    } catch (SQLException e) {
      throw new SqlExecuteException(sql, e);
    }
    return records;
  }

  private AbstractSqlBuilder select(String... columns) {
    AbstractSqlBuilder sql = new TSqlBuilder();
    if (columns == null || columns.length == 0) {
      sql.select(String.format("%s.*", name));
    } else {
      sql.select(Seq.map(Arrays.asList(columns), String.format("%s.%%s", name)).toArray(new String[0]));
    }
    sql.from(name);
    if (foreignTable != null && !foreignTable.isEmpty()) {
      sql.join(foreignTable);
    }
    if (!foreignKeys.isEmpty()) {
      for (String condition : getForeignKeys()) {
        sql.addCondition(condition);
      }
    }
    return sql;
  }

  private Record one(List<Record> models) {
    if (models.isEmpty()) {
      return null;
    } else {
      return models.get(0);
    }
  }

  public Record first() {
    return one(query(select().orderBy(primaryKey.concat(" asc")).limit(1).toString()));
  }

  public Record last() {
    return one(query(select().orderBy(primaryKey.concat(" desc")).limit(1).toString()));
  }

  public Record find(int id) {
    return one(where(String.format("%s = %d", primaryKey, id)));
  }

  public List<Record> findBy(String key, Object value) {
    key = key.toLowerCase();
    if (key.endsWith(":")) {
      key = key.substring(0, key.length() - 1);
    }
    if (value != null) {
      return where(key.concat(" = ?"), value);
    } else {
      return where(key.concat(" is null"));
    }
  }

  public List<Record> all() {
    return query(select().orderBy(primaryKey.concat(" asc")).toString());
  }

  public List<Record> where(String condition, Object... args) {
    return query(select().addCondition(condition).orderBy(primaryKey.concat(" asc")).toString(), args);
  }
}

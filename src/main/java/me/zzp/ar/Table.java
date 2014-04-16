package me.zzp.ar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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
  private String[] sort;

  Table(DB dbo, String name, Map<String, Integer> columns, Map<String, Association> relations) {
    this.dbo = dbo;
    this.name = name;
    this.columns = columns;
    this.relations = relations;
    this.primaryKey = name.concat(".id");
    this.sort = new String[] { primaryKey };
  }

  /* Association */
  private Association assoc(String name, boolean onlyOne, boolean ancestor) {
    name = DB.parseKeyParameter(name);
    Association assoc = new Association(relations, name, onlyOne, ancestor);
    relations.put(name, assoc);
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
    foreignKeys.put(DB.parseKeyParameter(key), id);
    return this;
  }

  public Table join(String table) {
    this.foreignTable = table;
    return this;
  }

  public Table sort(String... columns) {
    sort = columns;
    return this;
  }

  /* CRUD */
  public Record create(Object... args) {
    Map<String, Object> data = new HashMap<String, Object>();
    data.putAll(foreignKeys);
    for (int i = 0; i < args.length; i += 2) {
      String key = DB.parseKeyParameter(args[i].toString());
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
        if (rs != null && rs.next()) {
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

  /**
   * 根据现有的Record创建新的Record.
   * 为跨数据库之间导数据提供便捷接口；同时也方便根据模板创建多条相似的纪录。
   * @param o Record对象
   * @return 根据参数创建的新的Record对象
   */
  public Record create(Record o) {
    List<Object> params = new LinkedList<Object>();
    for (String key : columns.keySet()) {
      if (!foreignKeys.containsKey(key)) {
        params.add(key);
        params.add(o.get(key));
      }
    }
    return create(params.toArray());
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
          String label = DB.parseKeyParameter(meta.getColumnLabel(i));
          values.put(label, rs.getObject(label));
        }
        records.add(new Record(this, values));
      }

      Statement call = rs.getStatement();
      rs.close();
      call.close();
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
    if (sort != null && sort.length > 0) {
      sql.orderBy(sort);
    } else {
      sql.orderBy(primaryKey);
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
    return one(query(select().limit(1).toString()));
  }
  
  public Record first(String condition, Object... args) {
    return one(query(select().addCondition(condition).limit(1).toString(), args));
  }
  
  private String[] reverse() {
    String[] reverse;
    if (sort != null && sort.length > 0) {
      reverse = new String[sort.length];
      for (int i = 0; i < sort.length; i++) {
        String order = sort[i].toLowerCase();
        if (order.matches(".*\\basc$")) {
          reverse[i] = sort[i].replaceAll("(?i)asc$", "desc");
        } else if (order.matches(".*\\bdesc$")) {
          reverse[i] = sort[i].replaceAll("(?i)desc$", "");
        } else {
          reverse[i] = sort[i].concat(" desc");
        }
      }
    } else {
      reverse = new String[] { primaryKey.concat(" desc") };
    }
    return reverse;
  }

  public Record last() {
    return one(query(select().orderBy(reverse()).limit(1).toString()));
  }

  public Record last(String condition, Object... args) {
    return one(query(select().addCondition(condition).orderBy(reverse()).limit(1).toString(), args));
  }

  public Record find(int id) {
    return one(where(String.format("%s = %d", primaryKey, id)));
  }

  /**
   * 根据指定列，返回符合条件的第一条记录.
   * @param key 要匹配的列名
   * @param value 要匹配的值
   * @return 返回符合条件的第一条记录
   */
  public Record findA(String key, Object value) {
    key = DB.parseKeyParameter(key);
    if (value != null) {
      return first(key.concat(" = ?"), value);
    } else {
      return first(key.concat(" is null"));
    }
  }

  public List<Record> findBy(String key, Object value) {
    key = DB.parseKeyParameter(key);
    if (value != null) {
      return where(key.concat(" = ?"), value);
    } else {
      return where(key.concat(" is null"));
    }
  }

  public List<Record> all() {
    return query(select().toString());
  }

  public List<Record> where(String condition, Object... args) {
    return query(select().addCondition(condition).toString(), args);
  }

  public List<Record> paging(int page, int size) {
    return query(select().limit(size).offset(page * size).toString());
  }
}

package me.zzp.ar;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import me.zzp.ar.sql.TSqlBuilder;

public class Query {
  private final Table table;
  private final TSqlBuilder sql;
  private final List<Object> params;

  Query(Table table) {
    this.table = table;
    this.sql = new TSqlBuilder();
    this.params = new LinkedList<Object>();
  }
  
  public List<Record> all() {
    return table.query(sql, params.toArray());
  }

  public Record one() {
    limit(1);
    List<Record> models = all();
    if (models == null || models.isEmpty()) {
      return null;
    } else {
      return models.get(0);
    }
  }
  
  Query select(String... columns) {
    sql.select(columns);
    return this;
  }
  
  Query from(String table) {
    sql.from(table);
    return this;
  }

  Query join(String table) {
    sql.join(table);
    return this;
  }

  public Query where(String condition, Object... params) {
    sql.addCondition(condition);
    if (params != null && params.length > 0)
      this.params.addAll(Arrays.asList(params));
    return this;
  }

  public Query groupBy(String... columns) {
    sql.groupBy(columns);
    return this;
  }

  public Query having(String... conditions) {
    sql.having(conditions);
    return this;
  }

  public Query orderBy(String... columns) {
    sql.orderBy(columns);
    return this;
  }

  public Query limit(int limit) {
    sql.limit(limit);
    return this;
  }

  public Query offset(int offset) {
    sql.offset(offset);
    return this;
  }
}

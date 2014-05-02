package me.zzp.ar;

import java.util.List;
import me.zzp.ar.sql.TSqlBuilder;

public class Query {
  private final Table table;
  private final TSqlBuilder sql;

  Query(Table table) {
    this.table = table;
    this.sql = new TSqlBuilder();
  }
  
  public List<Record> all(Object... params) {
    return table.query(sql, params);
  }

  public Record one(Object... params) {
    limit(1);
    List<Record> models = all(params);
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

  public Query where(String condition) {
    sql.addCondition(condition);
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

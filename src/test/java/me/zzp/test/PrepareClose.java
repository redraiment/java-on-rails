package me.zzp.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;

public class PrepareClose {

  public static void main(String[] args) throws Exception {
    Connection base = DriverManager.getConnection("jdbc:sqlite::memory:");
    base.prepareStatement("create table users (id integer primary key autoincrement, name text, age int, created_at timestamp default current_timestamp, updated_at timestamp)").execute();

    Timestamp now = new Timestamp(System.currentTimeMillis());

    PreparedStatement call = base.prepareStatement("insert into users (name, age, updated_at) values (?, ?, ?)");
    call.setString(1, "Joe");
    call.setInt(2, 26);
    call.setTimestamp(3, now);
    call.addBatch();
    call.setString(1, "redraiment");
    call.setInt(2, 26);
    call.setString(3, now.toString());
    call.addBatch();
    call.executeBatch();

    ResultSet rs = base.prepareStatement("select * from users").executeQuery();
    ResultSetMetaData title = rs.getMetaData();
    for (int i = 1; i <= title.getColumnCount(); i++)
      System.out.print(title.getColumnLabel(i) + ",");
    System.out.println();

    while (rs.next()) {
      for (int i = 1; i <= title.getColumnCount(); i++)
        System.out.print(rs.getString(i) + ",");
      System.out.println();
    }

    rs.close();
    call.close();
    base.close();
  }
}

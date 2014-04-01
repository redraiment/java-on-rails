package me.zzp.test;

import java.io.IOException;
import me.zzp.ar.DB;
import me.zzp.ar.Record;
import me.zzp.ar.Table;

public class Sqlite3 {

  public static void main(String[] args) throws IOException {
    DB sqlite3 = DB.open("jdbc:sqlite::memory:");

    Table User = sqlite3.createTable("users",
                                     "name text",
                                     "age int");
    User.hasMany("blogs").by("author_id");

    Table Blog = sqlite3.createTable("blogs",
                                     "author_id int",
                                     "title text",
                                     "content text");
    Blog.belongsTo("author").by("author_id").in("users");

    User.create("name:", "Joe", "age:", 26);
    User.create("name:", "Christine", "age:", 27);
    User.create("name:", "Leon", "age:", 28);
    User.create("name:", "Kewell", "age:", 32);

    for (Record r : User.all()) {
      System.out.println(r);
    }
    System.out.println("---");

    Record joe = User.find(1);
    Table jobBlog = joe.get("blogs");
    jobBlog.create("title", "first blog",
                   "content", "hello world");

    Record leon = User.find(3);
    Table leonBlog = leon.get("blogs");
    leonBlog.create("content", "Hello jactiverecord",
                    "title", "Leon's blog");

    for (Record r : jobBlog.all()) {
      System.out.println(r);
      r.destroy();
    }
    System.out.println("---");

    for (Record r : Blog.all()) {
      System.out.print(r);
      Record author = r.get("author");
      System.out.printf("author: %s\n", author.get("name"));
    }
    System.out.println("---");

    joe.set("age", 25);
    joe.save();
    System.out.println(User.first());

    sqlite3.close();
  }
}

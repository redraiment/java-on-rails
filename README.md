# jActiveRecord
 
`jActiveRecord`是我根据自己的喜好用`Java`实现的对象关系映射（ORM）库，灵感来自`Ruby on Rails`的`ActiveRecord`。它拥有以下特色：

1. 零配置：无XML配置文件、无Annotation注解。
1. 零依赖：不依赖任何第三方库，运行环境为Java 6或以上版本。
1. 零SQL：无需显式地写任何SQL语句，甚至多表关联、分页等高级查询亦是如此。 
1. 动态性：和其他库不同，无需为每张表定义一个相对应的静态类。表、表对象、行对象等都能动态创建和动态获取。
1. 简化：`jActiveRecord`虽是模仿`ActiveRecord`，它同时做了一些简化。例如，所有的操作仅涉及DB、Table和Record三个类，并且HasMany、HasAndBelongsToMany等关联对象职责单一化，容易理解。
1. 支持多数据库访问
1. 多线程安全
1. 支持事务

# 入门

参考[Rails For Zombies](http://railsforzombies.org/)，我们一步一步创建一套僵尸微博系统的数据层。

## 安装

`jActiveRecord`采用`Maven`维护，并已发布到中央库，仅需在`pom.xml`中添加如下声明：

```xml
<dependency>
  <groupId>me.zzp</groupId>
  <artifactId>jactiverecord</artifactId>
  <version>2.3</version>
</dependency>
```

## 连接数据库

`jActiveRecord`的入口是`me.zzp.ar.DB`类，通过open这个静态方法创建数据库对象，open方法的参数与`java.sql.DriverManager#getConnection`兼容。

```java
DB sqlite3 = DB.open("jdbc:sqlite::memory:");
```

`DB#open`内部会自动创建连接池，为每个现成创建一个独立的连接对象，避免多线程间事务冲突；`DB#open`也能接收`DataSource`对象，即不使用自带的连接池，而使用`C3P0`等更成熟的第三方连接池实现。

作为演示，此处用sqlite创建一个内存数据库。

## 创建表

首先要创建一张用户信息表，此处的用户当然是僵尸（Zombie），包含名字（name）和墓地（graveyard）两个信息。

```java
Table Zombie = sqlite3.createTable("zombies", "name text", "graveyard text");
```

`createTable`方法的第一个参数是数据库表的名字，之后可以跟随任意个描述字段的参数，格式是名字+类型，用空格隔开。

`createTable`方法会自动添加一个自增长（auto increment）的`id`字段作为主键。由于各个数据库实现自增长字段的方式不同，目前`jActiveRecord`的“创建表”功能支持如下数据库：

* HyperSQL
* MySQL
* PostgreSQL
* SQLite

如果你使用的数据库不在上述列表中，可以自己实现`me.zzp.ar.d.Dialect`接口，并添加到`META-INF/services/me.zzp.ar.d.Dialect`。`jActiveRecord`采用`Java 6`的`ServiceLoader`自动加载实现`Dialect`接口的类。

此外`jActiveRecord`还会额外添加`created_at`和`updated_at`两个字段，类型均为`timestamp`，分别保存记录被创建和更新的时间。因此，上述代码总共创建了5个字段：`id`、`name`、`graveyard`、`created_at`和`updated_at`。

## 添加

```java
Table Zombie = sqlite3.active("zombies");
Zombie.create("name:", "Ash", "graveyard:", "Glen Haven Memorial Cemetery");
Zombie.create("name", "Bob", "graveyard", "Chapel Hill Cemetery");
Zombie.create("graveyard", "My Fathers Basement", "name", "Jim");
```

首先用`DB#active`获取之前创建的表对象，然后使用`Table#create`新增一条记录（并且立即返回刚创建的记录）。该方法可使用“命名参数”，来突显每个值的含义。由于Java语法不支持命名参数，因此列名末尾允许带一个冗余的冒号，即“name:”与“name”是等价的；此外键值对顺序无关，因此第三条名为“Jim”的僵尸记录也能成功创建。

## 查询

`jActiveRecord`提供了下列查询方法：

* `Record first()`：返回第一条记录。
* `Record first(String condition, Object... args)`：根据指定列的值第一条记录（允许为null）。
* `Record last()`：返回最后一条记录。
* `Record find(int id)`：返回指定`id`的记录。
* `Record findA(String condition, Object... args)`：根据条件返回第一条记录。
* `List<Record> findBy(String key, Object value)`：根据指定列的值查询（允许为null）。
* `List<Record> all()`：返回所有记录。
* `List<Record> where(String condition, Object... args)`：指定负责的过滤条件，兼容`java.sql.PreparedStatement`。
* `List<Record> paging(int page, int size)`：分页查询，`page`从`0`开始。

`first`、`last`和`find`等方法仅返回一条记录；另一些方法可能返回多条记录，因此返回`List`。

例如，获得`id`为3的僵尸有以下方法：

```java
Zombie.find(3);
Zombie.findBy("name", "Jim");
Zombie.where("graveyard like ?", "My Father%");
```

数据库返回的记录被包装成`Record`对象，使用`Record#get`获取数据。借助泛型，能根据左值自动转换数据类型：

```java
Record jim = Zombie.find(3);
int id = jim.get("id");
String name = jim.get("name");
Timestamp createdAt = jim.get("created_at");
```

此外，`Record`同样提供了诸如`getInt`、`getStr`等常用类型的强制转换接口。

`jActiveRecord`不使用`Bean`，因为`Bean`不通用，你不得不为每张表创建一个相应的`Bean`类；使用`Bean`除了能在编译期检查`getter`和`setter`的名字是否有拼写错误，没有任何好处；

## 更新

通过查询获得目标对象，接着可以做一些更新操作。例如将编号为3的僵尸的目的改成“Benny Hills Memorial”。

调用`Record#set`方法可更新记录中的值，然后调用`Record#save`或`Table#update`保存修改结果；或者调用`Record#update`一步完成更新和保存操作，该方法和`create`一样接受任意多个命名参数。

```java
Record jim = Zombie.find(3);
jim.set("graveyard", "Benny Hills Memorial").save();
jim.update("graveyard:", "Benny Hills Memorial"); // Same with above
```

## 删除

`Table#delete`和`Record#destroy`都能删除一条记录，`Table#purge`能删除当前约束下所有的记录。

```java
Zombie.find(1).destroy();
Zombie.delete(Zombie.find(1)); // Same with above
```

上述代码功能相同：删除`id`为1的僵尸。

## 关联

到了最精彩的部分了！ORM库除了将记录映射成对象，还要将表之间的关联信息面向对象化。

`jActiveRecord`提供与RoR一样的四种关联关系，并做了简化：

* Table#belongsTo
* Table#hasOne
* Table#hasMany
* Table#hasAndBelongsToMany

每个方法接收一个字符串参数`name`作为关系的名字，并返回`Association`关联对象，拥有以下三个方法：

* by：指定外键的名字，默认使用`name` + "_id"作为外键的名字。
* in：指定关联表的名字，默认与`name`相同。
* through：关联组合，参数为其他已经指定的关联的名字。即通过其他关联实现跨表访问（`join`多张表）。

### 一对多

回到僵尸微博系统的问题上，上面的章节仅创建了一张用户表，现在创建另一张表`tweets`保存微博信息：

```java
Table Tweet = sqlite3.createTable("tweets", "zombie_id int", "content text");
```

其中`zombie_id`作为外键与`zombies`表的`id`像关联。即每个僵尸有多条相关联的微博，而每条微博仅有一个相关联的僵尸。`jActiveRecord`中用`hasMany`和`belongsTo`来描述这种“一对多”的关系。其中`hasMany`在“一”方使用，`belongsTo`在“多”放使用（即外键所在的表）。

```java
Zombie.hasMany("tweets").by("zombie_id");
Tweet.belongsTo("zombie").by("zombie_id").in("zombies");
```

接着，就能通过关联名从`Record`中获取关联对象了。例如，获取`Jim`的所有微博：

```java
Record jim = Zombie.find(3);
Table jimTweets = jim.get("tweets");
for (Record tweet : jimTweets.all()) {
  // ...
}
```

或者根据微博获得相应的僵尸信息：

```java
Record zombie = Tweet.find(1).get("zombie");
```

你可能已经注意到了：`hasMany`会返回多条记录，因此返回`Table`类型；`belongsTo`永远只返回一条记录，因此返回`Record`。此外，还有一种特殊的一对多关系：`hasOne`，即“多”方有且仅有一条记录。`hasOne`的用法和`hasMany`相同，只是返回值是`Record`而不是`Table`。

### 关联组合

让我们再往微博系统中加入“评论”功能：

```java
Table Comment = sqlite3.createTable("comments", "zombie_id int", "tweet_id", "content text");
```

一条微博可以收到多条评论；而一个僵尸有多条微博。因此，僵尸和收到的评论是一种组合的关系：僵尸`hasMany`微博`hasMany`评论。`jActiveRecord`提供`through`描述这种组合的关联关系。

```java
Zombie.hasMany("tweets").by("zombie_id"); // has defined above
Zombie.hasMany("receive_comments").by("tweet_id").through("tweets");
Zombie.hasMany("send_comments").by("zombie_id").in("comments");
```

上面的规则描述了`Zombie`首先能找到`Tweet`，借助`Tweet.tweet_id`又能找到`Comment`。第三行代码描述`Zombie`通过`Comment`的`zombie_id`可直接获取发出去的评论。

事实上，`through`可用于组合任意类型的关联，例如`hasAndBelongsToMany`依赖`hasOne`、`belongsTo`依赖另一条`belongsTo`……

### 多对多

RoR中多对多关联有`has_many through`和`has_and_belongs_to_many`两种方法，且功能上有重叠之处。`jActiveRecord`仅保留`hasAndBelongsToMany`这一种方式来描述多对多关联。多对多关联要求有一张独立的映射表，记录映射关系。即两个“多”方都没有包含彼此的外键，而是借助第三张表同时保存它们的外键。

例如，为每条微博添加所在城市的信息，而城市单独作为一张表。

```java
sqlite3.dropTable("tweets");
Tweet = sqlite3.createTable("tweets", "zombie_id int", "city_id int", "content text");
Table City = sqlite3.createTable("cities", "name text");
```

其中表`cities`包含所有城市的信息，`tweets`记录僵尸和城市的关联关系。`Zombie`为了自己去过的`City`，它首先要连接到表`tweets`，再通过它访问`cities`。

```java
Zombie.hasMany("tweets").by("zombie_id"); // has defined above
Zombie.hasAndBelongsToMany("travelled_cities").by("city_id").in("cities").through("tweets");
```

顾名思义，多对多的关联返回的类型一定是`Table`而不是`Record`。

### 关联总结

* 一对一：有外键的表用`belongsTo`；无外键的表用`hasOne`。
* 一对多：有外键的表用`belongsTo`；无外键的表用`hasMany`。
* 多对多：两个多方都用`hasAndBelongsToMany`；映射表用`belongsTo`。

通过`through`可以任意组合其他关联。

# 总结

本文通过一个微博系统的例子，介绍了`jActiveRecord`的常用功能。

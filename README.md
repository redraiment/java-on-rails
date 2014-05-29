# jActiveRecord-EL

`jActiveRecord-EL`是[jActiveRecord](https://github.com/redraiment/jactiverecord)的辅助项目，简化在EL表达式中访问数据的方法，做到像操作普通`JavaBean`一样操作`Record`和`Table`类型的对象。适合采用了`jActiveRecord`的`Web`项目。

* 项目主页：[http://github.com/redraiment/jactiverecord-el](http://github.com/redraiment/jactiverecord-el)
* javadoc：[http://zzp.me/jactiverecord-el/](http://zzp.me/jactiverecord-el/)
* jActiveRecord：[http://github.com/redraiment/jactiverecord](http://github.com/redraiment/jactiverecord)

`jActiveRecord-EL`同样使用`Maven`管理，在`pom.xml`中添加如下依赖即可：

```xml
<dependency>
  <groupId>me.zzp</groupId>
  <artifactId>jactiverecord-el</artifactId>
  <version>1.1</version>
</dependency>
```

# 访问Record属性

假设`Record`实例`user`有一个字符串类型的属性`name`，如果不使用`jActiveRecord-EL`，要在EL表达式中获得该属性的值，方法是：

```xml
<h1>${user.get("name")}</h1>
```

采用`jActiveRecord-EL`之后，方法是：

```xml
<h1>${user.name}</h1>
```

`jActiveRecord-EL`简化了在EL表达式中访问`Record`属性的方法，能像访问`JavaBean`属性一样地访问`Record`的数据。

# 访问Table方法

`jActiveRecord-EL`同样简化了访问`Table`对象的方法，支持`all`、`first`、`last`和索引四种查询方式：

* `all`：调用`Table#all()`。即`${User.all}`等价于`${User.all()}`
* `first`：调用`Table#first()`。即`${User.first}`等价于`${User.first()}`
* `last`：调用`Table#last()`。即`${User.last}`等价于`${User.last()}`
* `索引`：调用`Table#find(int id)`。即`${User[1]}`等价于`${User.find(1)}`

*注意* `${User[1]}`与`${User.all[1]}`的意义并不相同，前者返回表中`id`等于1的记录；后者返回所有记录（all）中第*二*条记录（索引从0开始）。

# 配置

## 增强EL表达式

要使用jActiveRecord-EL，需要在`web.xml`中添加如下信息：

```xml
<listener>
  <listener-class>me.zzp.ar.el.ResolverSetup</listener-class>
</listener>
```

## 骆驼命名法（可选）

`JavaBean`属性的命名规则为骆驼命名法，例如“createdAt”；而数据库表的字段通常采用下划线命名法，例如“created_at”。在`web.xml`中添加如下上下文参数即可开启自动转换开关，之后就能使用骆驼命名法访问属性，即`${user.created_at}`与`${user.createdAt}`等价。

```xml
<context-param>
  <param-name>jactiverecord-el-camel-case</param-name>
  <param-value>true</param-value>
</context-param>
```

## 创建数据库对象（可选）

在`web`项目中使用`jActiveRecord`，通常第一步就是通过数据源（`javax.sql.DataSource`）创建数据库对象（`me.zzp.ar.DB`）。因此`jActiveRecord-EL`提供了另一个上下文监听器，在启动服务器的时候自动创建数据库对象，并添加到上下文对象的属性中，设置方法如下：

```xml
<listener>
  <listener-class>me.zzp.ar.el.DatabaseSetup</listener-class>
</listener>
<context-param>
  <param-name>jactiverecord-el-data-source</param-name>
  <param-value>java:/comp/env/jdbc/DataSource</param-value>
</context-param>
```

## 重命名属性名（可选）

`DatabaseSetup`创建的上下文属性名默认为“dbo”，即在`Servlet`中通过`getServletContext().getAttribute("dbo")`获得数据库对象。如果你不喜欢“dbo”这个名字，可指定以下信息自定义属性名：

```xml
<context-param>
  <param-name>jactiverecord-el-attribute-name</param-name>
  <param-value>database</param-value>
</context-param>
```

这样，属性名就改成了database。
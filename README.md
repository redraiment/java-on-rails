# jActiveRecord-EL

`jActiveRecord-EL`是[jActiveRecord](https://github.com/redraiment/jactiverecord)的辅助项目，简化在EL表达式中访问数据的方法，做到像操作不同`JavaBean`一样操作`Record`和`Table`类型的对象。适合采用了`jActiveRecord`的`Web`项目。

# 安装

下载最新的[jactiverecord.jar](https://github.com/redraiment/jactiverecord)和[jactiverecord-el.jar](https://github.com/redraiment/jactiverecord-el)，添加如下信息到`web.xml`：

```xml
<listener>
  <listener-class>me.zzp.ar.el.ResolverSetup</listener-class>
</listener>
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

# 骆驼命名法自动转换

`JavaBean`属性的命名规则为骆驼命名法，例如“createdAt”；而数据库表的字段通常采用下划线命名法，例如“created_at”。

开启了自动转换开关后就能将采用骆驼命名法的属性名自动转换成下划线命名法，即`${user.created_at}`与`${user.createdAt}`等价。在`web.xml`中添加如下上下文参数即可开启该选项：

```xml
<context-param>
  <param-name>jactiverecord-el-camel-case</param-name>
  <param-value>true</param-value>
</context-param>
```

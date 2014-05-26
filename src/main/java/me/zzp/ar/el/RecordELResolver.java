package me.zzp.ar.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.el.ELContext;
import javax.el.ELResolver;
import me.zzp.ar.Record;

/**
 * <p><code>me.zzp.ar.Record</code>的EL表达式解析器。
 * 简化<code>Record</code>对象方法调用的方式。</p>
 * @author redraiment
 * @since 1.0
 */
public final class RecordELResolver extends ELResolver {
  private final boolean camelCase;

  public RecordELResolver() {
    this(false);
  }

  public RecordELResolver(boolean camelCase) {
    this.camelCase = camelCase;
  }

  private String getKey(Object property) {
    String key = property.toString();
    return camelCase? key.replaceAll("(?=[A-Z])", "_").toLowerCase(): key;
  }

  /**
   * <p>像访问普通JavaBean一样访问Record中的字段。</p>
   * <p><code>${user.name}</code>等价于<code>${user.get("name")}</code>。</p>
   * @param context EL表达式上下文
   * @param base Table对象
   * @param property 属性
   * @return 返回相应属性的值
   */
  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    if (base != null && base instanceof Record && property != null) {
      context.setPropertyResolved(true);
      Record record = (Record) base;
      return record.get(getKey(property));
    } else {
      context.setPropertyResolved(false);
      return null;
    }
  }

  /**
   * 获取属性在数据库中相应的类型。
   * @param context EL表达式上下文
   * @param base Table对象
   * @param property 属性
   * @return 返回相应属性的类型
   */
  @Override
  public Class<?> getType(ELContext context, Object base, Object property) {
    if (base != null && base instanceof Record && property != null) {
      context.setPropertyResolved(true);
      Record record = (Record) base;
      Object o = record.get(getKey(property));
      return o == null? null: o.getClass();
    } else {
      context.setPropertyResolved(false);
      return null;
    }
  }

  /**
   * <p>设置属性值。</p>
   * <p>等价于调用<code>Record#set(String name, Object value)</code>。</p>
   * @param context EL表达式上下文
   * @param base Table对象
   * @param property 属性
   * @param value 值
   */
  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    if (base != null && base instanceof Record && property != null) {
      context.setPropertyResolved(true);
      Record record = (Record) base;
      record.set(getKey(property), value);
    } else {
      context.setPropertyResolved(false);
    }
  }

  /**
   * 均可写。
   * @param context EL表达式上下文
   * @param base Table对象
   * @param property 属性
   * @return false
   */
  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    if (base != null && base instanceof Record && property != null) {
      context.setPropertyResolved(true);
      return false;
    } else {
      context.setPropertyResolved(false);
      return false;
    }
  }

  /**
   * 返回由列名组成的列表。
   * @param context EL表达式上下文
   * @param base Table对象
   * @return 包含当前表的所有列名
   */
  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    List<FeatureDescriptor> list = new ArrayList<FeatureDescriptor>();
    if (base != null && base instanceof Record) {
      Record record = (Record) base;
      for (String column : record.columnNames()) {
        FeatureDescriptor feature = new FeatureDescriptor();
        feature.setDisplayName(column);
        feature.setName(column);
        feature.setShortDescription(column);
        feature.setHidden(false);
        feature.setExpert(false);
        feature.setPreferred(true);
        list.add(feature);
      }
    }
    return list.iterator();
  }

  /**
   * 属性为字符串类型。
   * @param context EL表达式上下文
   * @param base Table对象
   * @return String.class
   */
  @Override
  public Class<?> getCommonPropertyType(ELContext context, Object base) {
    return String.class;
  }
}

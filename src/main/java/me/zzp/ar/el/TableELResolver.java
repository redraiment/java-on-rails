package me.zzp.ar.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.el.ELContext;
import javax.el.ELResolver;
import me.zzp.ar.Record;
import me.zzp.ar.Table;

/**
 * <p><code>me.zzp.ar.Table</code>的EL表达式解析器。
 * 简化<code>Table</code>对象方法调用的方式。</p>
 * @author redraiment
 * @since 1.0
 */
public final class TableELResolver extends ELResolver {
  /**
   * <p>支持<code>all</code>、<code>first</code>、<code>last</code>和索引四种查询方式：</p>
   * <ul>
   *   <li>all：调用<code>Table#all()</code>。即<code>${User.all}</code>等价于<code>${User.all()}</code></li>
   *   <li>first：调用<code>Table#first()</code>。即<code>${User.first}</code>等价于<code>${User.first()}</code></li>
   *   <li>last：调用<code>Table#last()</code>。即<code>${User.last}</code>等价于<code>${User.last()}</code></li>
   *   <li>索引：调用<code>Table#find(int id)</code>。即<code>${User[1]}</code>等价于<code>${User.find(1)}</code></li>
   * </ul>
   * @param context EL表达式上下文
   * @param base Table对象
   * @param property 属性
   * @return 返回相应的方法调用结果
   */
  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    if (base != null && base instanceof Table && property != null) {
      context.setPropertyResolved(true);
      Table table = (Table) base;
      if (property instanceof String) {
        if (property.equals("all")) {
          return table.all();
        } else if (property.equals("first")) {
          return table.first();
        } else if (property.equals("last")) {
          return table.last();
        }
      } else if (property instanceof Number) {
        Number index = (Number) property;
        return table.find(index.intValue());
      }
      return null;
    } else {
      context.setPropertyResolved(false);
      return null;
    }
  }

  /**
   * 如果属性值为<code>all</code>，则返回 {@link java.util.List} 类型；
   * 否则返回 {@link me.zzp.ar.Record} 类型。
   * @param context EL表达式上下文
   * @param base Table对象
   * @param property 属性
   * @return 返回相应属性的类型
   */
  @Override
  public Class<?> getType(ELContext context, Object base, Object property) {
    if (base != null && base instanceof Table && property != null) {
      context.setPropertyResolved(true);
      if (property instanceof String) {
        if (property.equals("all")) {
          return List.class;
        } else if (property.equals("first") || property.equals("last")) {
          return Record.class;
        }
      } else if (property instanceof Number) {
        return Record.class;
      }
      return null;
    } else {
      context.setPropertyResolved(false);
      return null;
    }
  }

  /**
   * Table不允许修改。
   * @param context EL表达式上下文
   * @param base Table对象
   * @param property 属性
   * @param value 值
   */
  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    context.setPropertyResolved(base != null && base instanceof Table);
  }

  /**
   * 总是返回true。
   * @param context EL表达式上下文
   * @param base Table对象
   * @param property 属性
   * @return true
   */
  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    if (base != null && base instanceof Table) {
      context.setPropertyResolved(true);
      return true;
    } else {
      context.setPropertyResolved(false);
      return false;
    }
  }

  /**
   * 仅返回all、first和last三个方法名。
   * @param context EL表达式上下文
   * @param base Table对象
   * @return 包含all、first和last三个方法名
   */
  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    List<FeatureDescriptor> list = new ArrayList<>();
    if (base != null && base instanceof Table) {
      for (String column : Arrays.asList("all", "first", "last")) {
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

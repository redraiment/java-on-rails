package me.zzp.jactiverecord.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.el.ELContext;
import javax.el.ELResolver;
import me.zzp.ar.Record;

public final class RecordELResolver extends ELResolver {
  private final boolean camelCase;

  public RecordELResolver(boolean camelCase) {
    this.camelCase = camelCase;
  }
  
  private String getKey(Object property) {
    String key = property.toString();
    return camelCase? key.replaceAll("(?=[A-Z])", "_").toLowerCase(): key;
  }

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

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    if (base != null && base instanceof Record && property != null) {
      context.setPropertyResolved(true);
      return property.equals("id");
    } else {
      context.setPropertyResolved(false);
      return false;
    }
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    List<FeatureDescriptor> list = new ArrayList<>();
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

  @Override
  public Class<?> getCommonPropertyType(ELContext context, Object base) {
    return String.class;
  }
}

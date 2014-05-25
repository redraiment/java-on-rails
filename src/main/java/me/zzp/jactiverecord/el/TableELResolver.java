package me.zzp.jactiverecord.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.el.ELContext;
import javax.el.ELResolver;
import me.zzp.ar.Record;
import me.zzp.ar.Table;

public final class TableELResolver extends ELResolver {
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

  @Override
  public Class<?> getType(ELContext context, Object base, Object property) {
    if (base != null && base instanceof Table && property != null) {
      context.setPropertyResolved(true);
      Table table = (Table) base;
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

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    context.setPropertyResolved(base != null && base instanceof Table);
  }

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

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    List<FeatureDescriptor> list = new ArrayList<>();
    if (base != null && base instanceof Table) {
      Table table = (Table) base;
      for (String column : table.getColumns().keySet()) {
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

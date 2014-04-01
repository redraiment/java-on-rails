package me.zzp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class Seq {
  public static <E> E[] array(E... args) {
    return args;
  }

  public static <E> List<E> list(E... args) {
    List<E> list = new ArrayList<E>();
    list.addAll(Arrays.asList(args));
    return list;
  }

  public static int[] concat(int[] a, int... b) {
    int[] c = new int[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

  public static <E> E[] concat(E[] a, E... b) {
    return merge(a, b);
  }

  public static String join(String delimiter, Object... args) {
    return join(Arrays.asList(args), delimiter);
  }

  public static String join(Collection<?> list, String delimiter) {
    if (list == null || list.isEmpty()) {
      return "";
    }
    if (delimiter == null) {
      delimiter = "";
    }

    StringBuilder s = new StringBuilder();
    boolean first = true;
    for (Object e : list) {
      if (first) {
        first = false;
      } else {
        s.append(delimiter);
      }
      s.append(e);
    }
    return s.toString();
  }

  public static <E> E[] merge(E[] a, E[] b) {
    List<E> list = merge(Arrays.asList(a), Arrays.asList(b));
    return list.toArray(a);
  }

  public static <E> List<E> merge(List<E> a, List<E> b) {
    List<E> list = new ArrayList<E>();
    list.addAll(a);
    list.addAll(b);
    return list;
  }

  public static <E> E[] remove(E[] a, E e) {
    List<E> list = remove(Arrays.asList(a), e);
    return (E[])list.toArray();
  }

  public static <E> List<E> remove(List<E> a, E e) {
    List<E> list = new ArrayList<E>();
    for (E o : a) {
      if (!o.equals(e)) {
        list.add(o);
      }
    }
    return list;
  }

  public static <E> E[] valuesAt(E[] a, int... indexes) {
    List<E> list = valuesAt(Arrays.asList(a), indexes);
    return (E[])list.toArray();
  }

  public static <E> List<E> valuesAt(List<E> from, int... indexes) {
    List<E> list = new ArrayList<E>();
    for (int i : indexes) {
      if (0 <= i && i < from.size()) {
        list.add(from.get(i));
      } else if (-from.size() <= i && i < 0) {
        list.add(from.get(from.size() + i));
      } else {
        list.add(null);
      }
    }
    return list;
  }
  
  public static int[] assignAt(int[] a, Integer[] indexes, int... values) {
    if (indexes.length != values.length) {
      throw new IllegalArgumentException(String.format("index.length(%d) != values.length(%d)", indexes.length, values.length));
    }
    for (int i = 0; i < indexes.length; i++) {
      int index = indexes[i];
      if (0 <= index && index < a.length) {
        a[index] = values[i];
      } else if (-a.length <= index && index < 0) {
        a[a.length + index] = values[i];
      } else {
        throw new ArrayIndexOutOfBoundsException(index);
      }
    }
    return a;
  }

  public static <E> E[] assignAt(E[] a, Integer[] indexes, E... values) {
    if (indexes.length != values.length) {
      throw new IllegalArgumentException(String.format("index.length(%d) != values.length(%d)", indexes.length, values.length));
    }
    for (int i = 0; i < indexes.length; i++) {
      int index = indexes[i];
      if (0 <= index && index < a.length) {
        a[index] = values[i];
      } else if (-a.length <= index && index < 0) {
        a[a.length + index] = values[i];
      } else {
        throw new ArrayIndexOutOfBoundsException(index);
      }
    }
    return a;
  }

  public static List<String> map(Collection<?> from, String format) {
    List<String> to = new ArrayList<String>(from.size());
    for (Object e : from) {
      to.add(String.format(format, e));
    }
    return to;
  }

  public static List<String> partition(Collection<String> from, int n, String delimiter) {
    List<String> to = new ArrayList<String>();
    List<String> buffer = new ArrayList<String>(n);
    for (String e : from) {
      buffer.add(e);
      if (buffer.size() >= n) {
        to.add(join(buffer, delimiter));
        buffer.clear();
      }
    }
    return to;
  }
}

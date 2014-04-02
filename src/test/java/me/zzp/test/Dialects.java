package me.zzp.test;

import java.util.ServiceLoader;
import me.zzp.ar.d.Dialect;

public class Dialects {
  public static void main(String[] args) {
    for (Dialect d : ServiceLoader.load(Dialect.class)) {
      System.out.println(d.getClass().getName());
    }
  }
}

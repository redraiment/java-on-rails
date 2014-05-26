package me.zzp.test;

import java.util.Scanner;
import me.zzp.ar.DB;
import me.zzp.ar.Table;

public class CountActiveConnection {
  public static void main(String[] args) throws InterruptedException {
    DB dbo = DB.open("jdbc:postgresql:code", "redraiment", "123");
    final Table People = dbo.active("person");
    for (int i = 0; i < 10; i++) {
      Thread th = new Thread(new Runnable() {
        @Override
        public void run() {
          People.first();
        }
      });
      th.start();
      th.join();
    }
    System.out.println("pause");
    Scanner cin = new Scanner(System.in);
    cin.next();
  }
}

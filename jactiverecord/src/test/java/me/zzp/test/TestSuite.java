package me.zzp.test;

import me.zzp.ar.sql.TSqlBuilderTest;
import me.zzp.util.SeqTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  SeqTest.class,
  TSqlBuilderTest.class
})
public class TestSuite {
}

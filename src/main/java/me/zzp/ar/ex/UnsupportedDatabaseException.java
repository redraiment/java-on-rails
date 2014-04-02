package me.zzp.ar.ex;

public class UnsupportedDatabaseException extends RuntimeException {
  public UnsupportedDatabaseException(String product) {
    super(String.format("Unsupported Database: %s", product));
  }
}

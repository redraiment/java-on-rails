package me.zzp.ar.ex;

public class TransactionException extends RuntimeException {
  public TransactionException(String message, Throwable cause) {
    super(message, cause);
  }
}

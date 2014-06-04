package me.zzp.jac.ex;

public final class IllegalPathException extends RuntimeException {
  public IllegalPathException(String path) {
    super(path);
  }
}

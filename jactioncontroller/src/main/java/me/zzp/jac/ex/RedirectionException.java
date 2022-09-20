package me.zzp.jac.ex;

public final class RedirectionException extends RuntimeException {
  public RedirectionException(String type, String path, Throwable e) {
    super(String.format("%s to %s failed", type, path), e);
  }
}

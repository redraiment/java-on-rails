package me.zzp.ar.ex;

public class UndefinedAssociationException extends RuntimeException {
  public UndefinedAssociationException(String name) {
    super(String.format("undefined association name: %s", name));
  }
}

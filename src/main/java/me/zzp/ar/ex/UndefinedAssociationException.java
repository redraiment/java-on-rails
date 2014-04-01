package me.zzp.ar.ex;

public class UndefinedAssociationException extends RuntimeException {
  public UndefinedAssociationException(String name) {
    super(String.format("association name; %s", name));
  }
}

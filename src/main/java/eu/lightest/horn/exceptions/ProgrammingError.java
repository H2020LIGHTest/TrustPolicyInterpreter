package eu.lightest.horn.exceptions;

public class ProgrammingError extends RuntimeException {
  public ProgrammingError(String msg){
    super(msg);
  }
}

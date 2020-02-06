package eu.lightest.horn.exceptions;

public class InfinityLoopException extends HornFailedException {
  public InfinityLoopException(String msg){
    super("Infinity loop error: " + msg);
  }
}

package eu.lightest.horn.error.type;

import eu.lightest.horn.error.HornError;

public class TypeError extends HornError {
  public TypeError(String msg, int line, int pos) {
    super(msg, line, pos);
  }
}

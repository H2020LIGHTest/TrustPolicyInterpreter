package eu.lightest.horn.exceptions;

import eu.lightest.horn.error.ErrorHandler;

public class TypeCheckingException extends HornFailedException {
  public TypeCheckingException(String msg){
    super(msg);
  }
}

package eu.lightest.horn.error;

import eu.lightest.horn.error.type.LexerError;
import eu.lightest.horn.error.type.SyntaxError;
import eu.lightest.horn.error.type.TypeError;

public class ErrorFactory {

    public HornError createLexerError ( String msg, int line, int pos ) {
        return new LexerError(msg, line, pos);
    }

    public HornError createSyntaxError ( String msg, int line, int pos ) {
        return new SyntaxError(msg, line, pos);
    }

  public HornError createTypeError(String msg, int line, int pos) {
    return new TypeError(msg, line,pos);
  }
}

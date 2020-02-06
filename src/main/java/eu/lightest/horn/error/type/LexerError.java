package eu.lightest.horn.error.type;

import eu.lightest.horn.error.HornError;

public class LexerError extends HornError {
    public LexerError(String msg, int line, int pos) {
        super(msg, line, pos);
    }
}

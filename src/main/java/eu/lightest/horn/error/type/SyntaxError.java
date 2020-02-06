package eu.lightest.horn.error.type;

import eu.lightest.horn.error.HornError;

public class SyntaxError extends HornError {
    public SyntaxError(String msg, int line, int pos) {
        super(msg, line, pos);
    }
}

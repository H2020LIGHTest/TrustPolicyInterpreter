package eu.lightest.horn.lex;

import eu.lightest.horn.HornLexer;
import org.antlr.v4.runtime.CharStream;

public class LexerFactory {
    public static HornLexer createHornLexer( CharStream stream ) {
        return new HornLexer( stream );
    }
}

package eu.lightest.horn.lex;

import eu.lightest.horn.HornLexer;
import org.antlr.v4.runtime.TokenStream;

import java.io.File;

public interface LexerInterface {
    HornLexer lexer(File file);
    HornLexer lexer(String code);

    HornLexer getLexer();
    TokenStream getTokenStream();
}

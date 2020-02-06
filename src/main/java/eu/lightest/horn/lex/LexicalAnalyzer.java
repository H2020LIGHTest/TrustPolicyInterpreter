package eu.lightest.horn.lex;

import eu.lightest.horn.HornLexer;
import eu.lightest.horn.error.ErrorHandler;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;

public class LexicalAnalyzer implements LexerInterface {

    protected HornLexer ourLexer;
    protected CommonTokenStream ourTokenStream;


    @Override
    public HornLexer lexer(File file) {

        ErrorHandler.getInstance().reset();
        try {
            InputStream inputStream = new ByteArrayInputStream(FileUtils.readFileToByteArray(file));

            CharStream stream = CharStreams.fromStream(inputStream);

            ourLexer = buildLexer( stream );

        } catch (IOException e) {
            e.printStackTrace();
            ourLexer = null;
        }


        return ourLexer;
    }

    @Override
    public HornLexer lexer(String code) {
        ErrorHandler.getInstance().reset();
        CharStream stream = CharStreams.fromString(code);
        return buildLexer(stream);
    }

    @Override
    public HornLexer getLexer() {
        return ourLexer;
    }

    @Override
    public TokenStream getTokenStream() {

        return ourTokenStream;
    }

    protected HornLexer buildLexer( CharStream stream ) {
        ourLexer = LexerFactory.createHornLexer( stream );
        installCustomErrorListenersForLexer( ourLexer );

        ourTokenStream = new CommonTokenStream( ourLexer );

        ourTokenStream.getNumberOfOnChannelTokens();

        return ourLexer;
    }


    protected void installCustomErrorListenersForLexer ( HornLexer lexer ) {
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ANTLRErrorListener() {

            public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
                ErrorHandler.getInstance().addLexerError(i, i1, s);
            }

            public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

            }

            public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

            }

            public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

            }
        });
    }
}

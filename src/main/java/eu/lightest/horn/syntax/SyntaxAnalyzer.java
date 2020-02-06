package eu.lightest.horn.syntax;

import eu.lightest.horn.HornParser;
import eu.lightest.horn.error.ErrorHandler;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.BitSet;

public class SyntaxAnalyzer implements SyntaxInterface {

    private HornParser ourParser = null;
    private ParseTree ourTree = null;

    @Override
    public HornParser syntax(TokenStream stream) {

        ourParser = new HornParser( stream );
        installCustomErrorListener( ourParser );
        ourTree = ourParser.program();

        return ourParser;
    }

    @Override
    public HornParser getParser() {
        return ourParser;
    }

    @Override
    public ParseTree getParseTree() {
        return ourTree;
    }

    private void installCustomErrorListener ( HornParser parser ) {

        parser.removeErrorListeners();
        parser.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
                ErrorHandler.getInstance().addSyntaxError( i, i1, s );
            }

            @Override
            public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

            }

            @Override
            public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

            }

            @Override
            public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

            }
        });

    }
}

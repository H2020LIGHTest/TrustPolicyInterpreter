package eu.lightest.horn.syntax;

import eu.lightest.horn.HornParser;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public interface SyntaxInterface {
    HornParser syntax(TokenStream stream);
    HornParser getParser();
    ParseTree getParseTree();
}

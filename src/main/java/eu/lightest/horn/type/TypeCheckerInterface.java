package eu.lightest.horn.type;

import eu.lightest.horn.AST.TplPolicy;
import eu.lightest.horn.HornParser;
import org.antlr.v4.runtime.tree.ParseTree;

public interface TypeCheckerInterface {
    ParseTree typeCheck(HornParser parser);
    TplPolicy typeCheck(ParseTree tree);

    ParseTree getParseTree();
}

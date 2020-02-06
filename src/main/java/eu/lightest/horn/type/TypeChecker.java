package eu.lightest.horn.type;

import eu.lightest.horn.AST.TplPolicy;
import eu.lightest.horn.HornParser;
import eu.lightest.horn.codegen.Walker;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class TypeChecker implements TypeCheckerInterface {

    private ParseTree ourTree = null;

    @Override
    public ParseTree typeCheck(HornParser parser) {
        TplPolicy program = typeCheck(parser.program());
        return getParseTree();
    }

    @Override
    public TplPolicy typeCheck(ParseTree tree) {
        ourTree = tree;

        ParseTreeWalker walker = new ParseTreeWalker();

        Walker listener = new Walker();
        walker.walk( listener, ourTree );

        return listener.getProgram();
    }

    @Override
    public ParseTree getParseTree() {
        return ourTree;
    }
}

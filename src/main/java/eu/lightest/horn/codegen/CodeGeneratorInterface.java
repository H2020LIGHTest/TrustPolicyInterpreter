package eu.lightest.horn.codegen;

import eu.lightest.horn.AST.TplPolicy;
import eu.lightest.horn.exceptions.HornFailedException;
import eu.lightest.horn.exceptions.InfinityLoopException;

public interface CodeGeneratorInterface {

    boolean generate(TplPolicy question, String inputVariable, TplPolicy policy) throws HornFailedException;
    //boolean generate(AstProgram question, AstProgram policy);
}

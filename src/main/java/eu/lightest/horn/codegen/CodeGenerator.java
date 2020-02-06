package eu.lightest.horn.codegen;


import eu.lightest.horn.AST.TplPolicy;
import eu.lightest.horn.AST.TplPredication;
import eu.lightest.horn.exceptions.HornFailedException;
import eu.lightest.horn.exceptions.TermEqualityException;

public class CodeGenerator implements CodeGeneratorInterface {

  @Override
  public boolean generate(TplPolicy query, String inputVariable, TplPolicy policy) throws HornFailedException {

    if (!query.mPossibleClauses.isEmpty()) {
      return policy.proveClause(query.mPossibleClauses.get(0), inputVariable);
    } else {
      throw new HornFailedException("No Valid query is given to the Interpreter!");
    }

  }

}

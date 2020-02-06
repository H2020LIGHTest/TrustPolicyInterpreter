package eu.lightest.horn.AST;

import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.HornFailedException;
import eu.lightest.horn.exceptions.InfinityLoopException;
import eu.lightest.horn.solver.TplSearch;
import eu.lightest.horn.solver.TplSolution;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class TplPolicy {


  private static Logger logger = Logger.getLogger(TplPolicy.class);

  public List<TplClause> mPossibleClauses = new ArrayList<>();
  private Integer mLoopCounter = -1;


  void debug(Object obj) {
    for (int i = 0; i < mLoopCounter; i++)
      logger.debug("\t");
    logger.debug(obj.toString());
  }

  /**
   * Executes the policy with the given query and specified input variables.
   *
   * @param query         The query.
   * @param inputVariable Specifies which variables in the query represent the input document stored in the ATV.
   * @return The result is true if the execution gives true as result. The result is false if the execution gives
   * false as a result or if there is an error in the execution.
   */
  // first call
  // TODO: Perhaps we should return the solution instead of a boolean?
  // TODO: Why don't we support a conjuction of predications as a query instead of just a single predication?
  // TODO: Why is the query a TplClause if we're just extracting the mHead anyway?
  public boolean proveClause(TplClause query, String inputVariable) throws HornFailedException {

//    debug("Query: " + query.mHead);
    logger.debug("Query: " + query.mHead);

    List<String> inputVariables = Arrays.asList(inputVariable);

    TplSolution solution = TplSearch.solve(
        mPossibleClauses,
        Collections.singletonList(query.mHead),
        inputVariables);

    if(Interpreter.recordRPxTranscript){
      try {
        String transcript;
        if(solution.isSolution()){
          transcript = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n";
        } else {
          transcript = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n";
        }
        if (solution.getEnvironmentRecord() != null){
          transcript += solution.getEnvironmentRecord().toTPTP();
        }
        saveTranscript(transcript);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (solution.isSolution()) {
      debug("Solution found: " + solution.getSolution() + "\n");
      return true;
    } else if (solution.isNoSolution()) {
      debug("No solution found\n");
      return false;
    } else if (solution.isError()) {
      debug("Error: " + solution.getErrorMessage() + "\n");

      if (solution.isDepthLimitExceededError()) {
        throw new InfinityLoopException(solution.getErrorMessage());
      }

      return false;
    }

    return false;
  }

  private static void saveTranscript(String transcript) throws IOException {
    File file = new File(Interpreter.recordRPxTranscriptLocation);
    file.getParentFile().mkdirs();
    try (FileOutputStream out = new FileOutputStream(file)) {
      out.write(transcript.getBytes());
    }
  }

  private void debugPrintSubstitutionList(List<TplClause> tplSubstitutionList) {
    for (TplClause c : tplSubstitutionList) {
      debug("+-- Value: " + c);
    }
  }


  public String toString() {

    String rv = new String();

    for (TplClause c : mPossibleClauses) {
      rv += c;
      rv += '\n';
    }

    return rv;
  }
}

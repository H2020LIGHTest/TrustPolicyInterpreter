package eu.lightest.horn.specialKeywords;

import eu.lightest.horn.AST.*;
import eu.lightest.horn.Interpreter;
import eu.lightest.horn.substitution.TplSubstitution;
import org.apache.log4j.Logger;

import java.util.List;

class Print extends SpecialKeyword {
  private static Logger logger = Logger.getLogger(Print.class);

  Print(TplPredication query) {
    super(query);
  }

  @Override
  public TplSubstitution execute() {
    // TODO: correct?
    boolean success = true;
    for (TplTerm term : mQuery.mTerms) {
      System.out.println("Horn says: " + term); //for testcases needed!
      logger.info("Horn says: " + term);
      success = Interpreter.mAtvApi.onPrint(term.getPrintObj());
    }

    return success ? new TplSubstitution() : null;
  }

  @Override
  public boolean check() {
    return true;
  }
}

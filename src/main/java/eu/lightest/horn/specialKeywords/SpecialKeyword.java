package eu.lightest.horn.specialKeywords;

import eu.lightest.horn.AST.TplClause;
import eu.lightest.horn.AST.TplPredication;
import eu.lightest.horn.exceptions.TermEqualityException;
import eu.lightest.horn.substitution.TplSubstitution;

import java.util.List;

public abstract class SpecialKeyword {
  final TplPredication mQuery;

  SpecialKeyword(TplPredication query) {
    this.mQuery = query;
  }

  public abstract TplSubstitution execute() throws TermEqualityException;

  public abstract boolean check();

  public boolean used(List<TplClause> usedClauses){
    return true;
  }

}

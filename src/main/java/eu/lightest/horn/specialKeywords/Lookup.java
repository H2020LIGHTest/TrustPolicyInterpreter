package eu.lightest.horn.specialKeywords;

import eu.lightest.horn.AST.*;
import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.TermEqualityException;
import eu.lightest.horn.substitution.TplSubstitution;
import eu.lightest.horn.substitution.Unification;

import java.util.ArrayList;
import java.util.List;

class Lookup extends SpecialKeyword  {
  private final TplTerm mFirst;
  private final TplTerm mSecond;

  Lookup(TplPredication query) {
    super(query);
    if (mQuery.mTerms.size() >= 2) {
      mFirst = mQuery.mTerms.get(0);
      mSecond = mQuery.mTerms.get(1);
    } else {
      mFirst = null;
      mSecond = null;
    }
  }

  @Override
  public TplSubstitution execute() throws TermEqualityException {
    try {
      List<String> objVal = ((TplTermObject) mFirst).mValue;
      List<String> out = new ArrayList<>();

      if (!Interpreter.mAtvApi.onLookup(objVal, out)) {
        return null;
      }

      return Unification.unify(mSecond, new TplTermObject(out));
    } catch(HornApiException ae){
      ae.printStackTrace();
      //TODO error handling
    }

    return null;
  }

  @Override
  public boolean check() {
    return mQuery.mTerms.size() == 2 && mFirst instanceof TplTermObject;
  }
}

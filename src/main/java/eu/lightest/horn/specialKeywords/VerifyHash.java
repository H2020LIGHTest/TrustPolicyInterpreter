package eu.lightest.horn.specialKeywords;

import eu.lightest.horn.AST.TplPredication;
import eu.lightest.horn.AST.TplTerm;
import eu.lightest.horn.AST.TplTermObject;
import eu.lightest.horn.Interpreter;
import eu.lightest.horn.substitution.TplSubstitution;

class VerifyHash extends SpecialKeyword {
  private final TplTerm mFirst;
  private final TplTerm mSecond;

  VerifyHash(TplPredication query) {
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
  public TplSubstitution execute() {
    try {
      TplTermObject object = (TplTermObject) mFirst;
      TplTermObject hash = (TplTermObject) mSecond;

      if (Interpreter.mAtvApi.onVerifyHash(object.mValue, hash.mValue)) {
        return new TplSubstitution();
      } else {
        return null;
      }
    } catch (HornApiException ae) {
      ae.printStackTrace();
      //TODO debug handling
    }

    return null;
  }

  @Override
  public boolean check() {
    return mQuery.mTerms.size() == 2
            && mFirst instanceof TplTermObject
            && mSecond instanceof TplTermObject;
  }
}

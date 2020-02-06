package eu.lightest.horn.specialKeywords;

import eu.lightest.horn.AST.*;
import eu.lightest.horn.Interpreter;
import eu.lightest.horn.substitution.TplSubstitution;

class Trustscheme extends SpecialKeyword  {
  private final TplTerm mFirst;
  private final TplTerm mSecond;

  Trustscheme(TplPredication query) {
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
      TplTermObject claim = (TplTermObject) mFirst;
      TplTermComposite key = (TplTermComposite) mSecond;

      // TODO: correct?
      if (Interpreter.mAtvApi.onTrustschemeCheck(claim.mValue, key.getId())) {
        return new TplSubstitution();
      } else {
        return null;
      }
    }catch (HornApiException ae) {
      ae.printStackTrace();
      //TODO debug handling
    }

    return null;
  }

  @Override
  public boolean check() {
    return mQuery.mTerms.size() == 2
            && mFirst instanceof TplTermObject
            && mSecond instanceof TplTermComposite
            && ((TplTermComposite) mSecond).mTerms.isEmpty();
  }
}

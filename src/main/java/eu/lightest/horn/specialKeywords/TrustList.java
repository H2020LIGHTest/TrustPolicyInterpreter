package eu.lightest.horn.specialKeywords;

import eu.lightest.horn.AST.TplPredication;
import eu.lightest.horn.AST.TplTerm;
import eu.lightest.horn.AST.TplTermObject;
import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.TermEqualityException;
import eu.lightest.horn.substitution.TplSubstitution;
import eu.lightest.horn.substitution.Unification;

import java.util.ArrayList;
import java.util.List;

class TrustList extends SpecialKeyword  {
  private final TplTerm mFirst;
  private final TplTerm mSecond;
  private final TplTerm mThird;

  TrustList(TplPredication query) {
    super(query);
    if (mQuery.mTerms.size() >= 3) {
      mFirst = mQuery.mTerms.get(0);
      mSecond = mQuery.mTerms.get(1);
      mThird = mQuery.mTerms.get(2);
    } else {
      mFirst = null;
      mSecond = null;
      mThird = null;
    }
  }

  @Override
  public TplSubstitution execute() throws TermEqualityException {
    try {
      TplTermObject domain = (TplTermObject) mFirst;
      TplTermObject cert = (TplTermObject) mSecond;
      List<String> out = new ArrayList<>();

      if (!Interpreter.mAtvApi.onTrustlist(domain.mValue, cert.mValue, out)) {
        return null;
      }

      return Unification.unify(mThird, new TplTermObject(out));
    } catch (HornApiException ae) {
      ae.printStackTrace();
      //TODO error handling
    }

    return null;
  }

  @Override
  public boolean check() {
    return mQuery.mTerms.size() == 3
            && mFirst instanceof TplTermObject
            && mSecond instanceof TplTermObject;
  }
}

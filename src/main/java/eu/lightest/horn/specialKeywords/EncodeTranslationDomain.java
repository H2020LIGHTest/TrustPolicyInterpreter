package eu.lightest.horn.specialKeywords;

import eu.lightest.horn.AST.*;
import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.TermEqualityException;
import eu.lightest.horn.substitution.TplSubstitution;
import eu.lightest.horn.substitution.Unification;

import java.util.ArrayList;
import java.util.List;

public class EncodeTranslationDomain  extends SpecialKeyword {
  private final TplTerm mFirst;
  private final TplTerm mSecond;
  private final TplTerm mThird;

  EncodeTranslationDomain(TplPredication query) {
    super(query);
    if (mQuery.mTerms.size() == 3) {
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
      TplTermObject claim = (TplTermObject) mFirst;
      TplTermComposite trustedScheme = (TplTermComposite) mSecond;
      List<String> out = new ArrayList<>();

      if (Interpreter.mAtvApi.onEncodeTranslationDomain(claim.mValue, trustedScheme.getId(), out)) {
        return Unification.unify(mThird, new TplTermObject(out));
      } else {
        return null;
      }
    } catch (HornApiException ae) {
      ae.printStackTrace();
      //TODO debug handling, done through false enough?
    }

    return null;
  }

  @Override
  public boolean check() {
    return mQuery.mTerms.size() == 3
        && mFirst instanceof TplTermObject
        && mSecond instanceof TplTermComposite;
  }
}

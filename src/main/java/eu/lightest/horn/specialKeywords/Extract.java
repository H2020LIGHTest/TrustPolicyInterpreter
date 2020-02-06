package eu.lightest.horn.specialKeywords;

import eu.lightest.horn.AST.*;
import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.TermEqualityException;
import eu.lightest.horn.substitution.TplSubstitution;
import eu.lightest.horn.substitution.Unification;

import java.util.ArrayList;
import java.util.List;

class Extract extends SpecialKeyword  {
  private final TplTerm mFirst;
  private final TplTerm mSecond;
  private final TplTerm mThird;

  Extract(TplPredication query) {
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
      List<String> objVal = ((TplTermObject) mFirst).mValue;
      String identifier = ((TplTermComposite) mSecond).getId();
      List<String> out = new ArrayList<>();

      if (identifier.equals("format") && mThird instanceof TplTermComposite)
        if (!Interpreter.mAtvApi.setFormat(new ArrayList<>(objVal),
          ((TplTermComposite) mThird).getId()))
          return null;

      if (!Interpreter.mAtvApi.onExtract(objVal, identifier, out)) {
        return null;
      }

      return Unification.unify(mThird, new TplTermObject(out));
    } catch(HornApiException ae){
      ae.printStackTrace();
      //TODO error messaging, handling done through false, will make interpretation fail
    }

    return null;

  }

  @Override
  public boolean check() {
    return mQuery.mTerms.size() == 3
            && mFirst instanceof TplTermObject
            && mSecond instanceof TplTermComposite
            && ((TplTermComposite) mSecond).mTerms.isEmpty()
            && (!((TplTermComposite) mSecond).getId().equals("format")
                || (mThird instanceof TplTermComposite) && ((TplTermComposite) mThird).mTerms.isEmpty());
  }

}

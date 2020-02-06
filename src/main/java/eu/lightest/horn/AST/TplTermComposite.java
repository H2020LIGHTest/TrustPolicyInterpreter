package eu.lightest.horn.AST;

import eu.lightest.horn.exceptions.ProgrammingError;
import eu.lightest.horn.specialKeywords.IAtvApiListener;

import java.util.*;
import java.util.stream.Collectors;

public class TplTermComposite extends TplTerm {
  private String mId = null;
  public List<TplTerm> mTerms = new ArrayList<>();

  public TplTermComposite(TplTermComposite copy, Map<TplTermVar, TplTermVar> reuse) {
    super(copy);
    setId(new String(copy.getId()));
    mTerms.addAll(copy.mTerms.stream().map(old -> old.clone(reuse)).collect(Collectors.toList()));
  }

  public TplTermComposite(String mId, List<TplTerm> mTerms) {
    setId(mId);
    mTerms.addAll(new ArrayList<>(mTerms));
  }

  public TplTermComposite(String mId) {
    setId(mId);
  }

  @Override
  public TplTermComposite clone(Map<TplTermVar, TplTermVar> reuse) {
    return new TplTermComposite(this, reuse);
  }

  public String toString() {
    String rv = new String();

    if (mId != null && !mId.isEmpty() && Character.isUpperCase(mId.charAt(0))) {
      rv += '\'' + mId + '\'';
    } else {
      rv += mId;
    }

    if (mTerms.size() != 0) {
      rv += "(";

      for (TplTerm t : mTerms) {
        rv += t;
        rv += ", ";
      }

      rv = rv.substring(0, rv.length() - 2);
      rv += ")";
    }
    return rv;
  }

  @Override
  public Set<String> getVariables() {
    Set<String> xs = new HashSet<>();

    for (TplTerm s : this.mTerms) {
      xs.addAll(s.getVariables());
    }

    return xs;
  }

  @Override
  public Set<String> getFunctionSymbols() {
    Set<String> fs = new HashSet<>();
    fs.add(mId);

    for (TplTerm s : this.mTerms) {
      fs.addAll(s.getFunctionSymbols());
    }

    return fs;
  }

  @Override
  public List<TplTermObject> getTplTermObjects() {
    List<TplTermObject> objs = new ArrayList<>();
    for (TplTerm t : mTerms) {
      objs.addAll(t.getTplTermObjects());
    }
    return objs;
  }

  public String getId() {
    return mId;
  }

  public List<TplTerm> getTerms() {
    return mTerms;
  }

  public void setId(String mId) {
    // NOTE: It is currently important that composite terms cannot represent integers,
    //       and so we require that they at least begin with alphabetic characters.
    //       This is because we would like for TplTermIntegers and TplTermComposites
    //       to represent different terms (that is, TplTermComposites should not
    //       represent integers), to avoid ambiguities and confusion in the rest of
    //       the interpreter. The regex below matches the current grammar which
    //       enforces this property. If the grammar is changed then the regex should
    //       be updated accordingly. This requirement should probably persist unless
    //       and until the TplTermInteger class is deprecated and removed.
    if (mId == null || !mId.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
      throw new ProgrammingError("Not a valid TplTermComposite identifier: " + mId);
    }

    this.mId = mId;
  }

  @Override
  public IAtvApiListener.PrintObj getPrintObj() {
    return new IAtvApiListener.PrintObj(this.toString());
  }
}

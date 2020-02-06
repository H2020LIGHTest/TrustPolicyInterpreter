package eu.lightest.horn.AST;

import java.util.*;

public class TplPredication extends TplExpression {
  public String mId = null;

  public TplPredication() {
  }

  public TplPredication(TplPredication copy, Map<TplTermVar, TplTermVar> reuse) {
    super(copy, reuse);
    this.mId = new String(copy.mId);
  }

  @Override
  public TplExpression clone(Map<TplTermVar, TplTermVar> reuse) {
    return new TplPredication(this, reuse);
  }

  @Override
  public Set<String> getVariables() {
    Set<String> xs = new HashSet<>();

    for (TplTerm t : mTerms) {
      xs.addAll(t.getVariables());
    }

    return xs;
  }

  @Override
  public Set<String> getFunctionSymbols() {
    Set<String> fs = new HashSet<>();

    for (TplTerm t : mTerms) {
      fs.addAll(t.getFunctionSymbols());
    }

    return fs;
  }

  @Override
  public List<TplTermObject> getTplTermObjects() {
    List<TplTermObject> objs = new ArrayList<>();
    for (TplTerm t: mTerms) {
      objs.addAll(t.getTplTermObjects());
    }
    return objs;
  }

  public String toString() {
    String rv = new String();

    rv += mId;
    rv += '(';

    if (mTerms.size() > 0) {
      for (TplTerm t : mTerms) {
        rv += t;
        rv += ", ";
      }
      rv = rv.substring(0, rv.length() - 2);
    }

    rv += ')';
    return rv;
  }

}

package eu.lightest.horn.AST;

import java.util.*;
import java.util.stream.Collectors;

public class TplClause {
  public TplExpression mHead = null;
  public List<TplExpression> mBody = new ArrayList<TplExpression>();

  public TplClause() {

  }

  public TplClause(TplClause copy) {
    Map<TplTermVar, TplTermVar> reuse = new HashMap<>();
    this.mHead = copy.mHead.clone(reuse);
    this.mBody.addAll(copy.mBody.stream().map(old -> old.clone(reuse)).collect(Collectors.toList()));
  }

  public void addFact(TplExpression mFact) {
    if (mHead == null)
      mHead = mFact;
    else
      mBody.add(mFact);
  }

  public String toString() {
    String rv = new String();

    rv += mHead;

    if (mBody.size() > 0) {
      rv += " :- ";

      for (TplExpression f : mBody) {
        rv += f;
        rv += ", ";
      }

      rv = rv.substring(0, rv.length() - 2);
    }

    rv += ".";

    return rv;
  }

  public TplClause clone() {
    return new TplClause(this);
  }

  public Set<String> getVariables() {
    Set<String> xs = new HashSet<>();

    if (mHead != null) {
      xs.addAll(mHead.getVariables());
    }

    if (mBody != null) {
      for (TplExpression s : mBody) {
        xs.addAll(s.getVariables());
      }
    }

    return xs;
  }

  public Set<String> getFunctionSymbols() {
    Set<String> fs = new HashSet<>();

    if (mHead != null) {
      fs.addAll(mHead.getFunctionSymbols());
    }

    if (mBody != null) {
      for (TplExpression s : mBody) {
        fs.addAll(s.getFunctionSymbols());
      }
    }

    return fs;
  }
}

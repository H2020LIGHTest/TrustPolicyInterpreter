package eu.lightest.horn.AST;

import eu.lightest.horn.exceptions.ProgrammingError;
import eu.lightest.horn.exceptions.RelopException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TplRelop extends TplExpression {
  public String mOp = null;

  public TplRelop(TplRelop copy, Map<TplTermVar, TplTermVar> reuse) {
    super(copy, reuse);
    this.mOp = copy.mOp;
  }

  public TplRelop() {
  }

  @Override
  public TplExpression clone(Map<TplTermVar, TplTermVar> reuse) {
    return new TplRelop(this, reuse);
  }

  @Override
  public Set<String> getVariables() {
    Set<String> xs = new HashSet<>();

    if (mTerms != null) {
      for (TplTerm t : mTerms) {
        xs.addAll(t.getVariables());
      }
    }

    return xs;
  }

  @Override
  public Set<String> getFunctionSymbols() {
    return Collections.emptySet();
  }

  public String toString() {
    String rv = new String();

    rv += mTerms.get(0);
    rv += mOp;
    rv += mTerms.get(1);

    return rv;
  }

  public boolean solve() throws RelopException {
    int v1;
    int v2;

    if (mTerms == null || mTerms.size() < 2) {
      throw new RelopException("Cannot solve Relop: " + this);
    }

    TplTerm t1 = mTerms.get(0);
    TplTerm t2 = mTerms.get(1);

    if (t1 instanceof TplTermInteger) {
      v1 = ((TplTermInteger) t1).mValue;
    } else if (t1 instanceof TplTermObject && (((TplTermObject) t1).getResolventAsInteger() != null)) {
      v1 = ((TplTermObject) t1).getResolventAsInteger();
    } else {
      throw new RelopException("Terms '" + t1 + ", " + t2 + "' are not meant for Relop! 2");
    }

    if (t2 instanceof TplTermInteger) {
      v2 = ((TplTermInteger) t2).mValue;
    } else if (t2 instanceof TplTermObject && (((TplTermObject) t2).getResolventAsInteger() != null)) {
      v2 = ((TplTermObject) t2).getResolventAsInteger();
    } else {
      throw new RelopException("Terms '" + t1 + ", " + t2 + "' are not meant for Relop! 3");
    }

    switch (mOp) {
      case "<":
        return v1 < v2;
      case ">":
        return v1 > v2;
      case "==":
        return v1 == v2;
      case "<=":
        return v1 <= v2;
      case ">=":
        return v1 >= v2;
      default:
        throw new ProgrammingError("to such thing for relop");
    }
  }
}

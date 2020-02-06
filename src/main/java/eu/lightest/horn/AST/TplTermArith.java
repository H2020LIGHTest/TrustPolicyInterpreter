package eu.lightest.horn.AST;

import eu.lightest.horn.exceptions.ProgrammingError;
import eu.lightest.horn.specialKeywords.IAtvApiListener;

import java.util.*;
import java.util.stream.Collectors;

public class TplTermArith extends TplTerm {
  public String mOp = null;
  public List<TplTerm> mTerms = new ArrayList<>();

  public TplTermArith(TplTermArith copy, Map<TplTermVar, TplTermVar> reuse) {
    super();
    this.mOp = new String(copy.mOp);
    this.mTerms.addAll(copy.mTerms.stream().map(old -> old.clone(reuse)).collect(Collectors.toList()));
  }

  public TplTermArith() {
  }

  @Override
  public TplTermArith clone(Map<TplTermVar, TplTermVar> reuse) {
    return new TplTermArith(this, reuse);
  }

  @Override
  public String toString() {
    return mTerms.get(0).toString() + mOp + mTerms.get(1).toString();
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

    for (TplTerm s : this.mTerms) {
      fs.addAll(s.getFunctionSymbols());
    }

    return fs;
  }

  public TplTermInteger solve() {
    TplTermInteger t = new TplTermInteger();
    int v1;
    int v2;

    if (mTerms == null || mTerms.size() != 2) {
      throw new ProgrammingError("arith is not well-formed");
    }

    TplTerm t1 = mTerms.get(0);
    TplTerm t2 = mTerms.get(1);

    if (t1 instanceof TplTermInteger) {
      v1 = ((TplTermInteger) t1).mValue;
    } else if (t1 instanceof TplTermObject && (((TplTermObject) t1).isParsableInteger())) {
      v1 = ((TplTermObject) t1).getResolventAsInteger();
    } else {
      throw new ProgrammingError("arith on non-integers not allowed");
    }

    if (t2 instanceof TplTermInteger) {
      v2 = ((TplTermInteger) t2).mValue;
    } else if (t2 instanceof TplTermObject && (((TplTermObject) t2).isParsableInteger())) {
      v2 = ((TplTermObject) t2).getResolventAsInteger();
    } else {
      throw new ProgrammingError("arith only allowed on integers");
    }

    switch (this.mOp) {
      case "-":
        t.mValue = v1 - v2;
        break;
      case "+":
        t.mValue = v1 + v2;
        break;
      case "*":
        t.mValue = v1 * v2;
        break;
      default:
        throw new ProgrammingError("no other arith allowed!");
    }

    return t;
  }

  @Override
  public List<TplTermObject> getTplTermObjects() {
    List<TplTermObject> objs = new ArrayList<>();
    for (TplTerm t: mTerms) {
      objs.addAll(t.getTplTermObjects());
    }
    return objs;
  }

  @Override
  public IAtvApiListener.PrintObj getPrintObj() {
    return new IAtvApiListener.PrintObj(this.toString());
  }
}

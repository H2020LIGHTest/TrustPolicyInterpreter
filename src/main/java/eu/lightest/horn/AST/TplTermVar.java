package eu.lightest.horn.AST;

import eu.lightest.horn.specialKeywords.IAtvApiListener;

import java.util.*;

public class TplTermVar extends TplTerm {
  public String mId = null;

  public TplTermVar(TplTermVar copy) {
    this.mId = new String(copy.mId);
  }

  public TplTermVar() {
    super();
  }

  @Override
  public TplTermVar clone(Map<TplTermVar, TplTermVar> reuse) {
    if (reuse.containsKey(this)) {
      return reuse.get(this);
    } else {
      TplTermVar clone = new TplTermVar(this);
      reuse.put(this, clone);
      return clone;
    }
  }

  @Override
  public String toString() {
    return mId;
  }

  @Override
  public Set<String> getVariables() {
    Set<String> xs = new HashSet<>();
    xs.add(this.mId);
    return xs;
  }

  @Override
  public Set<String> getFunctionSymbols() {
    return Collections.emptySet();
  }

  @Override
  public List<TplTermObject> getTplTermObjects() {
    return Collections.emptyList();
  }

  @Override
  public IAtvApiListener.PrintObj getPrintObj() {
    return new IAtvApiListener.PrintObj((mId != null)? mId : "Uninitialized Variable");
  }
}

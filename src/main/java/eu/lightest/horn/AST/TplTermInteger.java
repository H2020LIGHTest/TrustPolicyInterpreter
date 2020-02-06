package eu.lightest.horn.AST;

import eu.lightest.horn.specialKeywords.IAtvApiListener;

import java.util.*;

public class TplTermInteger extends TplTerm {
  public Integer mValue;

  public TplTermInteger(TplTermInteger copy) {
    this.mValue = copy.mValue;
  }

  public TplTermInteger() {
  }

  public TplTermInteger(Integer value) {
    this.mValue = value;
  }

  @Override
  public TplTermInteger clone( Map<TplTermVar, TplTermVar> reuse) {
    return new TplTermInteger(this);
  }

  public String toString() {
    return mValue.toString();
  }

  @Override
  public Set<String> getVariables() {
    return new HashSet<>();
  }

  @Override
  public List<TplTermObject> getTplTermObjects() {
    return Collections.emptyList();
  }

  @Override
  public Set<String> getFunctionSymbols() {
    return Collections.emptySet();
  }

  @Override
  public IAtvApiListener.PrintObj getPrintObj() {
    return new IAtvApiListener.PrintObj(mValue.toString());
  }
}

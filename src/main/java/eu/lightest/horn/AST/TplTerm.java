package eu.lightest.horn.AST;

import eu.lightest.horn.specialKeywords.IAtvApiListener;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TplTerm {

  public TplTerm(TplTerm copy) {
  }

  public TplTerm() {
  }

  public abstract TplTerm clone( Map<TplTermVar, TplTermVar> reuse);

  public abstract String toString();

  public abstract Set<String> getVariables();

  public abstract List<TplTermObject> getTplTermObjects();

  public abstract Set<String> getFunctionSymbols();

  public abstract IAtvApiListener.PrintObj getPrintObj();
}

package eu.lightest.horn.AST;

import java.util.*;
import java.util.stream.Collectors;

public abstract class TplExpression {
    public List<TplTerm> mTerms = new ArrayList<>();

    public TplExpression(TplExpression copy, Map<TplTermVar, TplTermVar> reuse){
        this.mTerms.addAll(copy.mTerms.stream().map(old -> old.clone(reuse)).collect(Collectors.toList()));
    }
    public TplExpression(){}

    public abstract TplExpression clone(Map<TplTermVar, TplTermVar> reuse);

    public abstract Set<String> getVariables();

    public List<TplTermObject> getTplTermObjects(){
      List<TplTermObject> objs = new ArrayList<>();
      for (TplTerm t: mTerms) {
        objs.addAll(t.getTplTermObjects());
      }
      return objs;
    }

  public abstract Set<String> getFunctionSymbols();
}

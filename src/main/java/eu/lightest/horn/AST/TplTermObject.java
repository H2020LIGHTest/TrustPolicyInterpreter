package eu.lightest.horn.AST;

import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.ProgrammingError;
import eu.lightest.horn.specialKeywords.IAtvApiListener;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class TplTermObject extends TplTerm {
  private static Logger logger = Logger.getLogger(TplTermObject.class);

  public List<String> getValue() {
    return mValue;
  }

  public List<String> mValue;

  private boolean isResolved;
  private Object resolvent;
  private String resolventType;

  private TplTermObject(TplTermObject source) {
    this.mValue = source.mValue;
    this.isResolved = source.isResolved;
    this.resolvent = source.resolvent;
    this.resolventType = source.resolventType;
  }

  public TplTermObject(List<String> list) {
    this.mValue = new ArrayList<>(list);
    this.isResolved = false;
    this.resolvent = null;
    this.resolventType = null;
  }

  @Override
  public TplTermObject clone(Map<TplTermVar, TplTermVar> reuse) {
    return new TplTermObject(this);
  }

  public String toString() {
    return mValue.stream()
      .map(s -> (s != null && !s.isEmpty() && Character.isUpperCase(s.charAt(0))) ? "'" + s + "'" : s)
      .collect(Collectors.toList())
      .toString();
  }

  @Override
  public Set<String> getVariables() {
    return Collections.emptySet();
  }

  @Override
  public Set<String> getFunctionSymbols() {
    return Collections.emptySet();
  }

  /**
   * Resolves the object by asking the ATV which value is stored at the object's path in the ATV.
   * This is only done if it hasn't been done before.
   */
  private void resolve() {
    if (!isResolved) {
      IAtvApiListener.ResolvedObj obj = Interpreter.mAtvApi.resolveObj(this.mValue);
      if (obj == null) {
        resolvent = null;
        resolventType = null;
        isResolved = true;
      } else {
        resolvent = obj.mValue;
        resolventType = obj.mType;
        isResolved = true;
      }
      logger.debug("TplTermObject " + this + " resolved as " + resolventType + " " + resolvent);
    }
  }

  /**
   * Checks if the object's path reaches a leaf note in the tree which will be built in the ATV.
   * @return
   */
  public boolean isLeaf() {
    resolve();
    return resolvent != null && resolventType != null;
  }

  private String getResolventType()  {
    resolve();
    return resolventType;
  }

  public Object getResolvent() {
    resolve();
    return resolvent;
  }

  public boolean isInteger() {
    resolve();
    return resolventType != null && resolventType.equals("INT");
  }

  public boolean isString() {
    resolve();
    return resolventType != null && resolventType.equals("STRING");
  }

  public List<TplTermObject> getTplTermObjects() {
    return Collections.singletonList(this);
  }

  /**
   * Only to be used for creating TPTP files for RPx.
   * If the object has been resolved it returns a term representing the resolvent. Otherwise null.
   * @return
   */
  public Object resolventForTPTPTranslation() {
    if (!isResolved) {
      return null;
    }

    if (isInteger() || isString()) {
      return getResolvent();
    }
    throw new ProgrammingError("Bug in making the TPL record. There are TplTermObject types not covered");
  }

  public Integer getResolventAsInteger() {
    if (isInteger()) {
      return (Integer) getResolvent();
    }

    if (isString()) {
      try {
        return Integer.parseInt((String) getResolvent());
      } catch (NumberFormatException exception) {
        return null;
      }
    }

    return null;
  }

  public String getResolventAsString() {
    if (isInteger()) {
      Integer n = (Integer) getResolvent();
      return n == null ? null : Integer.toString(n);
    }

    if (isString()) {
      Object o = getResolvent();
      return o == null ? null : (String) getResolvent();
    }

    return null;
  }

  public boolean isParsableInteger() {
    return getResolventAsInteger() != null;
  }

  @Override
  public IAtvApiListener.PrintObj getPrintObj() {
    return new IAtvApiListener.PrintObj(this.mValue);
  }
}

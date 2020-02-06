package eu.lightest.horn.substitution;

import eu.lightest.horn.AST.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Substitutions represent maps from variables to terms.
 */
public class TplSubstitution {
  private Map<String, TplTerm> subst;

  private TplSubstitution(Map<String, TplTerm> subst) {
    this.subst = subst;
  }

  public TplSubstitution() {
    this(new HashMap<>());
  }

  public TplSubstitution(String x, TplTerm t) {
    this();
    put(x, t);
  }

  public TplSubstitution(TplTermVar x, TplTerm t) {
    this(x.mId, t);
  }

  private TplSubstitution put(String x, TplTerm t) {
    if (t != null && !(t instanceof TplTermVar && ((TplTermVar) t).mId.equals(x))) {
      subst.put(x, t);
    }
    return this;
  }

  public TplSubstitution update(String x, TplTerm t) {
    return new TplSubstitution(new HashMap<>(subst)).put(x, t);
  }

  public TplSubstitution update(TplTermVar x, TplTerm t) {
    return update(x.mId, t);
  }

  public Set<String> domain() {
    Set<String> dom = new HashSet<>(subst.keySet());
    Set<String> rem = new HashSet<>();
    for (String x : dom) {
      TplTerm t = subst.get(x);
      if (t == null || (t instanceof TplTermVar && (((TplTermVar) t).mId.equals(x)))) {
        rem.add(x);
      }
    }
    dom.removeAll(rem);
    return dom;
  }

  public List<TplTerm> range() {
    List<TplTerm> range = new ArrayList<>();
    for (String x : domain()) {
      range.add(apply(x));
    }
    return range;
  }

  public Set<String> rangeVariables() {
    Set<String> vars = new HashSet<>();
    for (TplTerm t : range()) {
      vars.addAll(t.getVariables());
    }
    return vars;
  }

  /**
   * Two substitutions can be composed by applying the one to all the variables of the terms in the
   * range of the other.
   * @param s The substitution to compose with.
   * @return The composition.
   */
  public TplSubstitution compose(TplSubstitution s) {
    TplSubstitution newsubst = new TplSubstitution();
    Stream.concat(domain().stream(), s.domain().stream())
            .forEach(x -> newsubst.put(x, s.apply(apply(x))));
    return newsubst;
  }

  /**
   * Applying a substitution to a variable is done by looking up the variable in the substitution
   * and returning the corresponding term. If no term is found the variable itself is returned.
   * @param x The variable
   * @return The application of the substitution to the variable.
   */
  public TplTerm apply(String x) {
    if (subst.containsKey(x)) {
      return subst.get(x).clone(new HashMap<>());
    } else {
      TplTermVar newx = new TplTermVar();
      newx.mId = x;
      return newx;
    }
  }

  /**
   * Applying a substitution to a term is done by applying the substitution to all variables in the term.
   * @param t The term
   * @return The application of the substitution to the term.
   */
  public TplTerm apply(TplTerm t) {
    if (t instanceof TplTermVar) {
      TplTermVar x = (TplTermVar) t;
      return subst.getOrDefault(x.mId, t).clone(new HashMap<>());
    } else if (t instanceof TplTermComposite) {
      TplTermComposite s = ((TplTermComposite) t).clone(new HashMap<>());
      s.mTerms = s.mTerms.stream().map(this::apply).collect(Collectors.toList());
      return s;
    } else if (t instanceof TplTermArith) {
      TplTermArith s = (TplTermArith) t.clone(new HashMap<>());
      s.mTerms = s.mTerms.stream().map(this::apply).collect(Collectors.toList());
      return s;
    } else {
      return t; // TODO: Is this correct?
    }
  }

  /**
   * Applying a substitution to an expression is done by applying the substitution to all variables in the expression.
   * @param e The expression
   * @return The application of the substitution to the expression.
   */
  public TplExpression apply(TplExpression e) {
    TplExpression newE; //TODO: Use a copy constructor instead or clone.
    if (e instanceof TplPredication) {
      newE = new TplPredication();
      ((TplPredication) newE).mId = ((TplPredication) e).mId;
    } else if (e instanceof TplRelop) {
      newE = new TplRelop();
      ((TplRelop) newE).mOp = ((TplRelop) e).mOp;
    } else {
      return null;
    }

    if (e.mTerms != null) {
      newE.mTerms = e.mTerms.stream().map(this::apply).collect(Collectors.toList());
    }

    return newE;
  }

  /**
   * Applying a substitution to a clause is done by applying the substitution to all variables in the clause.
   * @param c The clause
   * @return The application of the substitution to the clause.
   */
  public TplClause apply(TplClause c) {
    TplClause newC = new TplClause();
    newC.mHead = apply(c.mHead);
    newC.mBody = apply(c.mBody);
    return newC;
  }

  public List<TplExpression> apply(List<TplExpression> es) {
    return es.stream().map(this::apply).collect(Collectors.toList());
  }

  public TplEquation apply(TplEquation eq) {
    return new TplEquation(apply(eq.lhs), apply(eq.rhs));
  }

  public TplSubstitution restrictTo(Collection<String> xs) {
    TplSubstitution newsubst = new TplSubstitution();

    for (String y : domain()) {
      TplTerm t = apply(y);

      Set<String> zs = t.getVariables().stream()
              .filter(xs::contains)
              .collect(Collectors.toSet());

      if (xs.contains(y) || !zs.isEmpty()) {
        newsubst.put(y, t);
      }
    }

    return newsubst;
  }

  @Override
  public String toString() {
    List<String> s = domain().stream().map((x) -> x + " -> " + apply(x)).collect(Collectors.toList());
    return "[" + String.join(", ", s) + "]";
  }
}

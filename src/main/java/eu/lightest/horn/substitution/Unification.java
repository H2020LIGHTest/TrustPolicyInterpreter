package eu.lightest.horn.substitution;

import eu.lightest.horn.AST.*;
import eu.lightest.horn.exceptions.ProgrammingError;
import eu.lightest.horn.exceptions.TermEqualityException;

import java.util.*;
import java.util.stream.Collectors;

public class Unification {
  /**
   * Checks if two terms are structurally equal. TplObjects are resolved before comparison if necessary.
   *
   * @param s The first term that we want to compare with.
   * @param t The second term that we want to compare with.
   * @return Whether or not the terms are equal.
   */
  static public boolean termEquals(TplTerm s, TplTerm t) throws TermEqualityException {
    if (s == t) {
      return true;
    }

    if (s == null || t == null) {
      return false;
    }

    // A variable is only equal to other variables with the same identifier
    if (s instanceof TplTermVar) {
      String sId = ((TplTermVar) s).mId;

      if (t instanceof TplTermVar) {
        String tId = ((TplTermVar) t).mId;
        return (sId == null && tId == null) || (sId != null && sId.equals(tId));
      }

      return false;
    }

    // The symmetric case for variables
    if (t instanceof TplTermVar) {
      return termEquals(t, s);
    }

    // An arithmetic expression is only equal to other arithmetic expressions with equal structure
    if (s instanceof TplTermArith) {
      TplTermArith a = (TplTermArith) s;

      if (t instanceof TplTermArith) {
        TplTermArith b = (TplTermArith) t;

        if (!a.mOp.equals(b.mOp) || a.mTerms.size() != b.mTerms.size()) {
          return false;
        }

        for (int i = 0; i < a.mTerms.size(); i++) {
          if (!termEquals(a.mTerms.get(i), b.mTerms.get(i))) {
            return false;
          }
        }

        return true;
      }

      return false;
    }

    // The symmetric case for arithmetic expressions
    if (t instanceof TplTermArith) {
      return termEquals(t, s);
    }

    // Integers are treated as constants (i.e., composite terms without subterms) for comparison purposes
    if (s instanceof TplTermInteger) {
      Integer n = ((TplTermInteger) s).mValue;

      if (t instanceof TplTermInteger) {
        Integer m = ((TplTermInteger) t).mValue;
        return (n == null && m == null) || (n != null && n.equals(m));
      }

      if (n == null) {
        return false;
      }

      String ns = Integer.toString(n);

      if (t instanceof TplTermComposite) {
        TplTermComposite t2 = (TplTermComposite) t;

        return ns.equals(t2.getId()) && t2.mTerms.isEmpty();
      }

      if (t instanceof TplTermObject) {
        TplTermObject tObj = (TplTermObject) t;

        return tObj.isLeaf() && ns.equals(tObj.getResolventAsString());
      }

      return false;
    }

    // The symmetric case for arithmetic expressions
    if (t instanceof TplTermInteger) {
      return termEquals(t, s);
    }

    // A term object is only equal to
    // 1. another term object with either an equal path or an equal resolvent
    // 2. other terms if its resolvent can be treated as a constant (i.e., a term composite without subterms)
    if (s instanceof TplTermObject) {
      TplTermObject sObj = (TplTermObject) s;
      String sStr = sObj.getResolventAsString();

      if (t instanceof TplTermObject) {
        TplTermObject tObj = (TplTermObject) t;

        // If they represent the same path then they are equal
        if (sObj.mValue.equals(tObj.mValue)) {
          return true;
        }

        // If they both represent leaf nodes then they are equal if and only if they contain equal data (as strings)
        if (sObj.isLeaf() && tObj.isLeaf()) {
          return sStr != null && sStr.equals(tObj.getResolventAsString());
        }

        // Otherwise they both represent paths that are different and that are not leaf nodes
        return false;
      }

      if (t instanceof TplTermComposite) {
        TplTermComposite t2 = (TplTermComposite) t;

        return sStr != null && sStr.equals(t2.getId()) && t2.mTerms.isEmpty();
      }

      return false;
    }

    // The symmetric case for term objects
    if (t instanceof TplTermObject) {
      return termEquals(t, s);
    }

    // A composite term is only equal to
    // 1. structurally equal composite terms, and to
    // 2. term objects representing constants (which is covered by previous cases)
    if (s instanceof TplTermComposite) {
      TplTermComposite sf = (TplTermComposite) s;

      if (t instanceof TplTermComposite) {
        TplTermComposite tf = (TplTermComposite) t;

        if (!sf.getId().equals(tf.getId()) || sf.mTerms.size() != tf.mTerms.size()) {
          return false;
        }

        for (int i = 0; i < sf.mTerms.size(); i++) {
          if (!termEquals(sf.mTerms.get(i), tf.mTerms.get(i))) {
            return false;
          }
        }

        return true;
      }

      return false;
    }

    // The symmetric case for composite terms
    if (t instanceof TplTermComposite) {
      return termEquals(t, s);
    }

    throw new ProgrammingError("Missing case in term equality check (bug?): " + s + " == " + t);
  }


  private Unification() {
  }

  /**
   * Implements the unification algorithm which calculates a most general unifier for the given
   * list of equations. See e.g. section 3.2.2 of
   *   Deransart, P., Ed-Dbali, A., & Cervoni, L. (1996). Prolog: The standard: Reference manual. Springer.
   *
   * @param equations The equations.
   * @return A most general unifier for the given set of equations.
   * @throws TermEqualityException Thrown if a comparison of objects with different non-leaf paths are done.
   */
  public static TplSubstitution unify(List<TplEquation> equations) throws TermEqualityException {
    TplSubstitution mgu = new TplSubstitution();
    Deque<TplEquation> cur = new ArrayDeque<>(equations);

    while (!cur.isEmpty()) {
      TplEquation e = cur.removeFirst();

      if (e == null || e.lhs == null || e.rhs == null) {
        throw new ProgrammingError("null equations in unification algorithm");
      }

     /* if (e.lhs instanceof TplTermArith || e.rhs instanceof TplTermArith) {
        throw new ProgrammingError("arithmetic expressions in unification algorithm: " + e);
      } */

      TplTerm s = e.lhs;
      TplTerm t = e.rhs;

      if (s instanceof TplTermVar) {
        if (termEquals(s, t)) {
          continue;
        }

        String x = ((TplTermVar) s).mId;

        if (t.getVariables().contains(x)) {
          return null;
        }

        TplSubstitution subst = mgu.compose(new TplSubstitution(x, t));
        cur = cur.stream().map(subst::apply).collect(Collectors.toCollection(ArrayDeque::new));
        mgu = subst;
      } else if (t instanceof TplTermVar) {
        cur.addFirst(new TplEquation(t, s));
      } else if (s instanceof TplTermComposite) {
        TplTermComposite sf = (TplTermComposite) s;

        if (t instanceof TplTermComposite) {
          TplTermComposite tf = (TplTermComposite) t;

          int sa = sf.mTerms.size();
          int ta = tf.mTerms.size();

          if (!sf.getId().equals(tf.getId()) || sa != ta) {
            return null;
          }

          for (int i = sa - 1; i >= 0; i--) {
            cur.addFirst(new TplEquation(sf.mTerms.get(i), tf.mTerms.get(i)));
          }
        } else if (!termEquals(s, t)) {
          return null;
        }
      } else if (t instanceof TplTermComposite) {
        cur.addFirst(new TplEquation(t, s));
      } else if (s instanceof TplTermInteger || s instanceof TplTermObject || s instanceof TplTermArith) {
        if (!termEquals(s, t)) {
          return null;
        }
      } else if (t instanceof TplTermInteger || t instanceof TplTermObject || t instanceof TplTermArith) {
        cur.addFirst(new TplEquation(t, s));
      } else {
        throw new ProgrammingError("unhandled case in unification algorithm (bug?): " + e);
      }
    }

    return mgu;
  }

  public static TplSubstitution unify(TplTerm term1, TplTerm term2) throws TermEqualityException{
    List<TplEquation> equations = new ArrayList<>();
    equations.add(new TplEquation(term1, term2));

    return unify(equations);
  }

  public static TplSubstitution unify(TplPredication predication1, TplPredication predication2) throws TermEqualityException{
    if (predication1 == null
            || predication2 == null
            || !predication1.mId.equals(predication2.mId)
            || predication1.mTerms.size() != predication2.mTerms.size()) {
      return null;
    }

    List<TplEquation> equations = new ArrayList<>();

    for (int i = 0; i < predication1.mTerms.size(); i++) {
      equations.add(new TplEquation(predication1.mTerms.get(i), predication2.mTerms.get(i)));
    }

    return unify(equations);
  }
}

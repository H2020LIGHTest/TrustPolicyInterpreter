import eu.lightest.horn.AST.*;
import eu.lightest.horn.exceptions.TermEqualityException;
import eu.lightest.horn.substitution.TplEquation;
import eu.lightest.horn.substitution.TplSubstitution;
import eu.lightest.horn.substitution.Unification;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestUnification {
  private void runTest(TplSubstitution expected, TplEquation... ts) {
    try {
      TplSubstitution mgu = Unification.unify(Arrays.asList(ts));

      if (expected == null || mgu == null) {
        org.junit.Assert.assertTrue(expected == mgu);
      } else {
        org.junit.Assert.assertTrue(
                "domain differs: got " + mgu.domain() + " but expected " + expected.domain(),
                mgu.domain().equals(expected.domain()));

        org.junit.Assert.assertTrue(
                "range differs: got " + mgu + " but expected " + expected,
                mgu.domain().stream().allMatch((x) -> {
                  try {
                    return Unification.termEquals(mgu.apply(x), expected.apply(x));
                  } catch (TermEqualityException e) {
                    org.junit.Assert.fail(e.getMessage());
                    return false;
                  }
                }));
      }
    } catch (TermEqualityException e) {
      org.junit.Assert.fail(e.getMessage());
    }
  }

  private void runTest(TplSubstitution expected, TplTerm s, TplTerm t) {
    runTest(expected, new TplEquation(s, t));
  }

  private void runTestFail(TplEquation... ts) {
    try {
      TplSubstitution s = Unification.unify(Arrays.asList(ts));
      org.junit.Assert.assertNull(s);
    } catch (TermEqualityException e) {
      org.junit.Assert.fail(e.getMessage());
    }
  }

  private void runTestFail(TplTerm s, TplTerm t) {
    runTestFail(new TplEquation(s, t));
  }

  @Test
  public void emptyList() {
    runTest(new TplSubstitution());
  }

  @Test
  public void equalVariables() {
    TplTermVar x = new TplTermVar();
    TplTermVar y = new TplTermVar();

    x.mId = "X";
    y.mId = "X";

    TplSubstitution subst = new TplSubstitution();

    runTest(subst, x, y);
  }

  @Test
  public void differentVariables() {
    TplTermVar x = new TplTermVar();
    TplTermVar y = new TplTermVar();

    x.mId = "X";
    y.mId = "Y";

    TplSubstitution subst = new TplSubstitution("X", y);

    runTest(subst, x, y);
  }

  @Test
  public void constants1() {
    TplTermComposite c = new TplTermComposite("c");
    TplTermVar x = new TplTermVar();

    x.mId = "X";

    TplSubstitution subst = new TplSubstitution("X", c);

    runTest(subst, c, x);
  }

  @Test
  public void constants2() {
    TplTermComposite c = new TplTermComposite("c");
    TplTermComposite d = new TplTermComposite("d");

    runTest(null, c, d);
  }

  @Test
  public void composedTerms1() {
    // s = f(c,X), t = f(Y,d), u = Z
    // s == t, t == u

    TplTermComposite c = new TplTermComposite("c");
    TplTermComposite d = new TplTermComposite("d");

    TplTermVar x = new TplTermVar();
    TplTermVar y = new TplTermVar();
    TplTermVar z = new TplTermVar();

    x.mId = "X";
    y.mId = "Y";
    z.mId = "Z";


    TplTermComposite s = new TplTermComposite("f");
    TplTermComposite t = new TplTermComposite("f");
    TplTerm u = z;

    s.mTerms.add(c);
    s.mTerms.add(x);

    t.mTerms.add(y);
    t.mTerms.add(d);

    TplSubstitution subst0 = new TplSubstitution().update(x, d).update(y, c);
    TplSubstitution subst = subst0.compose(new TplSubstitution(z, subst0.apply(t)));

    runTest(subst, new TplEquation(s, t), new TplEquation(t, u));
  }

  @Test
  public void composedTerms2() {
    // s = f(X,Y), t = f(c,X)
    // s == t

    TplTermComposite c = new TplTermComposite("c");

    TplTermVar x = new TplTermVar();
    TplTermVar y = new TplTermVar();

    x.mId = "X";
    y.mId = "Y";

    TplTermComposite s = new TplTermComposite("f");
    TplTermComposite t = new TplTermComposite("f");

    s.mTerms.add(x);
    s.mTerms.add(y);

    t.mTerms.add(c);
    t.mTerms.add(x);

    runTest(new TplSubstitution().update(x, c).update(y, c), s, t);
  }

  @Test
  public void composedTerms3() {
    // s = f(X,Y), t = f(c,g(Z,Z))
    // s == t

    TplTermComposite c = new TplTermComposite("c");

    TplTermVar x = new TplTermVar();
    TplTermVar y = new TplTermVar();
    TplTermVar z = new TplTermVar();

    x.mId = "X";
    y.mId = "Y";
    z.mId = "Z";

    TplTermComposite s = new TplTermComposite("f");
    TplTermComposite t = new TplTermComposite("f");
    TplTermComposite u = new TplTermComposite("g");

    s.mTerms.add(x);
    s.mTerms.add(y);

    u.mTerms.add(z);
    u.mTerms.add(z);

    t.mTerms.add(c);
    t.mTerms.add(u);

    runTest(new TplSubstitution().update(x, c).update(y, u), s, t);
  }

  @Test
  public void composedTerms4() {
    // t = f(X,X)
    // X == t

    TplTermVar x = new TplTermVar();

    x.mId = "X";

    TplTermComposite t = new TplTermComposite("f");

    t.mTerms.add(x);
    t.mTerms.add(x);

    runTest(null, x, t);
  }

  @Test
  public void composedTerms5() {
    // s = f(X), t = f(X,X)
    // s == t

    TplTermVar x = new TplTermVar();

    x.mId = "X";

    TplTermComposite s = new TplTermComposite("f");
    TplTermComposite t = new TplTermComposite("f");

    s.mTerms.add(x);

    t.mTerms.add(x);
    t.mTerms.add(x);

    runTest(null, s, t);
  }


  @Test
  public void termObjects1() {
    List<String> as = new ArrayList<>();
    as.add("transaction");
    as.add("bid");

    List<String> bs = new ArrayList<>();
    bs.add("transaction");
    bs.add("bid");
    bs.add("amount");

    TplTermObject a = new TplTermObject(as);
    TplTermObject b = new TplTermObject(bs);

    runTest(new TplSubstitution(), a, a);
    runTest(new TplSubstitution(), b, b);
  }

  @Test
  public void termObjects2() {
    List<String> as = new ArrayList<>();
    as.add("transaction");
    as.add("bid");

    List<String> bs = new ArrayList<>();
    bs.add("transaction");
    bs.add("bid");
    bs.add("amount");

    TplTermObject a = new TplTermObject(as);
    TplTermObject b = new TplTermObject(bs);

    runTestFail(a, b);
    runTestFail(b, a);
  }

  @Test
  public void termIntegers() {
    TplTermInteger a = new TplTermInteger(0);
    TplTermInteger b = new TplTermInteger(1);

    runTest(null, a, b);
    runTest(new TplSubstitution(), a, a);
    runTest(null, b, a);
    runTest(new TplSubstitution(), b, b);
  }
}
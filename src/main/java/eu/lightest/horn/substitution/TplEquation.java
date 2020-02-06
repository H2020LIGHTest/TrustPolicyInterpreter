package eu.lightest.horn.substitution;

import eu.lightest.horn.AST.TplTerm;

public class TplEquation {
  final TplTerm lhs; // left-hand side of the equation
  final TplTerm rhs; // right-hand side of the equation

  public TplEquation(TplTerm lhs, TplTerm rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public String toString() {
    return "equation(" + lhs + ", " + rhs + ")";
  }
}
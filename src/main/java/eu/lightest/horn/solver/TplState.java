package eu.lightest.horn.solver;

import eu.lightest.horn.AST.TplExpression;
import eu.lightest.horn.exceptions.ProgrammingError;
import eu.lightest.horn.substitution.TplSubstitution;

import java.util.Deque;

public class TplState {
  final Deque<TplExpression> query;
  final TplSubstitution partialSolution;
  final Integer currentDepth;

  public static TplState initialState(Deque<TplExpression> query, TplSubstitution initialSubst) {
    return new TplState(query, initialSubst, 0);
  }

  public static TplState initialState(Deque<TplExpression> query) {
    return initialState(query, new TplSubstitution());
  }

  private TplState(Deque<TplExpression> query,
                   TplSubstitution partialSolution,
                   Integer currentDepth) {
    if (query == null || partialSolution == null || currentDepth == null) {
      throw new ProgrammingError("TplState should not contain nulls");
    }

    this.query = query;
    this.partialSolution = partialSolution;
    this.currentDepth = currentDepth;
  }

  public TplState cloneWith(Deque<TplExpression> newQuery) {
    return new TplState(newQuery, this.partialSolution, this.currentDepth+1);
  }

  public TplState cloneWith(TplSubstitution newPartialSolution) {
    return new TplState(this.query, newPartialSolution, this.currentDepth+1);
  }

  public TplState cloneWith(Deque<TplExpression> newQuery,
                            TplSubstitution newPartialSolution) {
    return new TplState(newQuery, newPartialSolution, this.currentDepth+1);
  }

  public boolean isLeaf() {
    return query.isEmpty();
  }

  @Override
  public String toString() {
    return "State(query: " + query + ", solution: " + partialSolution + ", depth: " + currentDepth + ")";
  }
}

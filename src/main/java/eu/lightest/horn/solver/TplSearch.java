package eu.lightest.horn.solver;

import eu.lightest.horn.AST.*;
import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.ProgrammingError;
import eu.lightest.horn.exceptions.RelopException;
import eu.lightest.horn.exceptions.TermEqualityException;
import eu.lightest.horn.rpx.EnvironmentRecord;
import eu.lightest.horn.specialKeywords.SpecialKeyword;
import eu.lightest.horn.specialKeywords.SpecialKeywordHandler;
import eu.lightest.horn.substitution.TplSubstitution;
import eu.lightest.horn.substitution.Unification;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class TplSearch {
  private static Logger logger = Logger.getLogger(TplSearch.class);

  private final static CharSequence VARIABLE_SUFFIX = "$";
  private final static int DEPTH_BOUND = 1000; // TODO: What value should we assign to this variable?


  // TODO: is there a better way?
  private static Deque<TplExpression> cloneQuery(Collection<TplExpression> query, TplSubstitution subst) {
    return query.stream()
            .map(e -> e.clone(new HashMap<>()))
            .map(subst::apply)
            .collect(Collectors.toCollection(ArrayDeque::new));
  }

  // TODO: is there a better way?
  private static Deque<TplExpression> cloneQuery(Collection<TplExpression> query) {
    return cloneQuery(query, new TplSubstitution());
  }

  private static TplClause createTplClause(TplExpression head, List<TplExpression> body) {
    TplClause clause = new TplClause();
    clause.mHead = head;
    clause.mBody = body;
    return clause;
  }

  private static TplPredication createTplFact(String id, List<TplTerm> terms) {
    TplPredication p = new TplPredication();
    p.mId = id;
    p.mTerms = terms;
    return p;
  }

  private static TplRelop createTplRelop(String op, List<TplTerm> terms) {
    TplRelop p = new TplRelop();
    p.mOp = op;
    p.mTerms = terms;
    return p;
  }

  private static List<TplTerm> evaluateArithmeticExpressions(List<TplTerm> terms) {
    return terms.stream().map(TplSearch::evaluateArithmeticExpressions).collect(Collectors.toList());
  }

  private static TplTerm evaluateArithmeticExpressions(TplTerm term) {
    if (term instanceof TplTermArith) {
      TplTermArith arith = (TplTermArith) term;
      List<TplTerm> subterms = evaluateArithmeticExpressions(arith.mTerms);
      if (subterms.size() != 2) {
        throw new ProgrammingError("Arithmetic term was not given exactly two arguments:" + arith);
      }

      if (!(subterms.get(0) instanceof TplTermInteger) || !(subterms.get(1) instanceof TplTermInteger)) {
        throw new ProgrammingError("Evaluation of arithmetic terms on non-integer terms is not supported: " + arith);
      }
      Integer left = ((TplTermInteger) subterms.get(0)).mValue;
      Integer right = ((TplTermInteger) subterms.get(1)).mValue;
      return new TplTermInteger(calculate(arith.mOp, left, right));
    } else if (term instanceof TplTermComposite) {
      TplTermComposite t = (TplTermComposite) term;
      TplTermComposite s = new TplTermComposite(t.getId());
      s.mTerms = t.mTerms.stream().map(TplSearch::evaluateArithmeticExpressions).collect(Collectors.toList());
      return s;
    }

    return term;
  }

  private static TplClause evaluateArithmeticExpressions(TplClause clause) {
    TplExpression newHead;
    if (clause.mHead instanceof TplPredication) {
      TplPredication head = (TplPredication) clause.mHead;
      newHead = createTplFact(head.mId, evaluateArithmeticExpressions(head.mTerms));
    } else if (clause.mHead instanceof TplRelop) { // TODO: Why do we have this case? Do we support TplRelop in the head of clauses?
      TplRelop head = (TplRelop) clause.mHead;
      newHead = createTplRelop(head.mOp, evaluateArithmeticExpressions(head.mTerms));
    } else {
      throw new ProgrammingError("unhandled case in solveClauseHead method (bug?): " + clause.mHead);
    }

    return createTplClause(newHead, new ArrayList<>(clause.mBody));
  }

  private static Integer calculate(String mOp, Integer v1, Integer v2) {
    switch (mOp) {
      case "-":
        return v1 - v2;
      case "+":
        return v1 + v2;
      case "*":
        return v1 * v2;
      default:
        throw new ProgrammingError("The arithmetic operator is not allowed: " + mOp);
    }
  }

  /**
   * Renames the variables in the clause such that they do not clash with the names
   * in the queryVars set. The state is used to choose a suitable name.
   * @param st The state.
   * @param clause The clause that we want to rename.
   * @param queryVars The variables that we want to avoid.
   * @return The renamed clause.
   */
  private static TplClause renameVars(TplState st, TplClause clause, Set<String> queryVars) {
    Set<String> clauseVars = clause.getVariables();

    Set<String> intersection = new HashSet<>(queryVars);
    intersection.retainAll(clauseVars);

    if (intersection.isEmpty()) {
      return clause.clone();
    }

    TplSubstitution renaming = new TplSubstitution();
    for (String x : intersection) {
      TplTermVar y = new TplTermVar();
      y.mId = x+VARIABLE_SUFFIX+st.currentDepth;
      renaming = renaming.update(x, y);
    }

    return renaming.apply(clause);
  }

  /**
   * Executes the program starting in the given state. The execution is done as a depth-first search in a
   * search tree consisting of TplStates. The search is successful if a state is found that contains an empty query.
   * The search is unsuccesful if no such state if found. The search can also result in an error. These cases
   * are recorded in the returned solution.
   *
   * In case an error happens during the search then this is recorded in the solution.
   *
   * For an explanation of this search, also called "search-tree visit and construction algorithm for definite Prolog"
   * see e.g.
   *   Deransart, P., Ed-Dbali, A., & Cervoni, L. (1996). Prolog: The standard: Reference manual. Springer.
   * in particular section 4.2. The book's Figure 4.2 shows an example of a search-tree.
   *
   * We adapt the algorithm to deal also with arithmetic expression and built-in predicates.
   * The idea is that for states where the query begins with a predication we will look at which heads of renamed
   * clauses in the policy unify with the predication and then for these clauses perform a step of resolution
   * to produce a new state. However, if the query begins with a predication calling a built-in predicate
   * (special keyword) the child is instead calculated by executing the built-in predicate. If the query begins
   * with an arithmetic expression then that expression is evaluated.
   *
   * @param st The state from which the search starts.
   * @param originalQueryVariables The variables that occurred in the original query.
   * @param program The program/policy.
   * @param skh The special
   * @return The result of the search representing whether it was successful, unsuccessful or gave an error.
   */
  private static TplSolution search(
          TplState st,
          Set<String> originalQueryVariables,
          List<TplClause> program,
          SpecialKeywordHandler skh, EnvironmentRecord env) throws RelopException {
    logger.debug(st.toString());
    if (st.currentDepth >= DEPTH_BOUND) {
      return TplSolution.depthLimitError("Depth bound exceeded");
    }

    // The partial solution represents a complete solution if we have reached the empty clause.
    if (st.isLeaf()) {
      // Filter out the freshly generated variables and return the solution
      return TplSolution.solution(st.partialSolution.restrictTo(originalQueryVariables), env);
    }

    // Otherwise we have to solve the next atom in the query.
    // The atom can express either
    // 1. a relation on arithmetic expressions,
    // 2. a call to a built-in/interpreted predicate with special semantics, or
    // 3. an atom with an ordinary predicate suitable for unification.
    Deque<TplExpression> qs = cloneQuery(st.query);
    TplExpression q = qs.removeFirst();

    q.mTerms = evaluateArithmeticExpressions(q.mTerms);
    if(env != null){
      env = env.recordClause(st.query.getFirst(), q);
      env = env.recordObjects(q.getTplTermObjects());
    }

    if (q instanceof TplRelop) {
      // The atom is a relation on arithmetic expressions.
      if (!q.getVariables().isEmpty()) {
        logger.debug("Relation on arithmetic expressions contains variables at evaluation point: " + q + ". Backtracking...");
        return TplSolution.noSolution(env);
      } else {
        try {
          if (((TplRelop) q).solve()) {
            if(env != null){
              env = env.recordComparison((TplRelop) q);
            }
            return search(st.cloneWith(qs), originalQueryVariables, program, skh, env);
          }
        } catch (RelopException e) {
          logger.debug("RelopException: " + e.getMessage() + ". Backtracking...");
          return TplSolution.noSolution(env);
        }
      }
      return TplSolution.noSolution(env);
    } else if (q instanceof TplPredication) {
      TplPredication qf = (TplPredication) q;

      SpecialKeyword skw = skh.detect((TplPredication) q);

      if (skw != null) {
        // The predicate of the atom is one of the special built-in predicates.
        if (skw.check()) {
          TplSubstitution subst = null;
          try {
            subst = skw.execute();
          } catch (TermEqualityException e){
            logger.debug("TermEqualityException: " + e.getMessage() + ". Backtracking...");
            return TplSolution.noSolution(env);
          }

          if (subst != null) {
            if (env != null) {
              env = env.recordObjects(subst.apply(qf).getTplTermObjects());
              env = env.recordFact((TplPredication) subst.apply(qf));
            }
            return search(st.cloneWith(cloneQuery(qs, subst), st.partialSolution.compose(subst)), originalQueryVariables, program, skh, env);
          } else {
            logger.debug("Built-in predicate call " + q + " failed to execute. Backtracking...");
            return TplSolution.noSolution(env);
          }
        } else {
          logger.debug("Built-in predicate call " + q + " failed its check. Backtracking...");
          return TplSolution.noSolution(env);
        }
      } else {
        // The predicate of the atom is "non-interpreted"/"ordinary"
        Set<String> vars = new HashSet<>();
        for (TplExpression e : st.query) {
          vars.addAll(e.getVariables());
        }
        vars.addAll(st.partialSolution.domain());
        vars.addAll(st.partialSolution.rangeVariables());

        for (TplClause clause : program) {
          TplClause arithEvaledClause = evaluateArithmeticExpressions(renameVars(st, clause, vars));

          TplSubstitution mgu;
          try {
            mgu = Unification.unify(qf, (TplPredication) arithEvaledClause.mHead);
          } catch (TermEqualityException e){
            return TplSolution.error(e.getMessage());
          }

          if (mgu != null) {
            Deque<TplExpression> newqs = cloneQuery(arithEvaledClause.mBody);
            newqs.addAll(qs);

            TplSolution sol = search(
                    st.cloneWith(cloneQuery(newqs, mgu), st.partialSolution.compose(mgu)),
                    originalQueryVariables, program, skh, env);

            if (sol.isSolution() || sol.isError()) {
              return sol;
            }
          }
        }
      }
    }

    // If q could not be solved then there's no solution for the current branch of the search tree
    return TplSolution.noSolution(env);
  }

  /**
   * Executes a policy/program with the given query and specified input variables. The method follows the
   * so-called "search-tree visit and construction algorithm for definite Prolog" extended with support for
   * external predicates, for TplTermObject and for arithmetics.
   * For an explanation of "search-tree visit and construction algorithm for definite Prolog" see e.g.
   *   Deransart, P., Ed-Dbali, A., & Cervoni, L. (1996). Prolog: The standard: Reference manual. Springer.
   * in particular section 4.2.
   *
   * @param program The TPL policy/program.
   * @param query The query.
   * @param inputVariables Specifies which variables in the query represent the input document stored in the ATV.
   * @return The solution resulting from the execution of the policy with the given query and input variables.
   */
  public static TplSolution solve(List<TplClause> program, List<TplExpression> query, List<String> inputVariables) throws RelopException {
    // Check that the program does not have relational operators in the head of clauses
    for (TplClause clause : program) {
      if (clause.mHead instanceof TplRelop) {
        throw new RelopException("You should not have relational operators in the head of a clause: " + clause);
      }
    }

    // Check that the variables of the program and the query are well-formed
    Set<String> queryVars = new HashSet<>();
    for (TplExpression e : query) {
      queryVars.addAll(e.getVariables());
    }

    Set<String> vars = new HashSet<>(queryVars);
    for (TplClause cl : program) {
      vars.addAll(cl.getVariables());
    }

    for (String x : vars) {
      if (x.contains(VARIABLE_SUFFIX)) {
        throw new ProgrammingError("Invalid variable name in program or query: " + x);
      }
    }

    // Instantiate the "input variables" to the special "input term object"
    /* List<TplExpression> updatedQuery = query;
    TplSubstitution subst = new TplSubstitution();
    if (!queryVars.isEmpty()
            && !query.isEmpty()
            && query.get(0) != null
            && !query.get(0).mTerms.isEmpty()
            && (query.get(0).mTerms.get(0) instanceof TplTermVar)) {

      TplTermVar x = (TplTermVar) query.get(0).mTerms.get(0);
      subst = new TplSubstitution(x, new TplTermObject(Collections.singletonList("input")));
      updatedQuery = query.stream().map(subst::apply).collect(Collectors.toList());
    } */
    // Alternatively:
    TplSubstitution subst = new TplSubstitution();
    for (String x : inputVariables) {
      if (queryVars.contains(x)) {
        subst = subst.update(x, new TplTermObject(Collections.singletonList("input")));
      }
    }
    List<TplExpression> updatedQuery = query.stream().map(subst::apply).collect(Collectors.toList());

    logger.debug("Original query: " + query);
    logger.debug("Updated query: " + updatedQuery);


    EnvironmentRecord env = null;
    if(Interpreter.recordRPxTranscript){
      List<TplClause> program2 = new ArrayList<>();
      for (TplClause c : program) {
        program2.add(evaluateArithmeticExpressions(c));
      }
      env = new EnvironmentRecord(updatedQuery, program2);
    }

    return search(
            TplState.initialState(cloneQuery(updatedQuery), subst),
            Collections.unmodifiableSet(queryVars),
            Collections.unmodifiableList(program),
            new SpecialKeywordHandler(),
            env);
  }
}

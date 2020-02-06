package eu.lightest.horn.solver;

import eu.lightest.horn.exceptions.ProgrammingError;
import eu.lightest.horn.rpx.EnvironmentRecord;
import eu.lightest.horn.substitution.TplSubstitution;

public class TplSolution {
  private enum SolutionVariant {SOLUTION, NO_SOLUTION, ERROR, ERROR_DEPTH_LIMIT_EXCEEDED}

  private final TplSubstitution subst;
  private final String errorMsg;
  private final SolutionVariant variant;
  private final EnvironmentRecord env;

  public static TplSolution noSolution(EnvironmentRecord env) {
    return new TplSolution(env);
  }

  public static TplSolution emptySolution() {
    return new TplSolution(new TplSubstitution(), null);
  }

  public static TplSolution depthLimitError(String errorMsg) {
    return new TplSolution(null, errorMsg, SolutionVariant.ERROR_DEPTH_LIMIT_EXCEEDED,null);
  }

  public static TplSolution solution(TplSubstitution subst, EnvironmentRecord env) {
    if (subst == null) {
      throw new ProgrammingError("The solution cannot be null");
    }

    return new TplSolution(subst,env);
  }

  public static TplSolution error(String errorMsg) {
    if (errorMsg == null) {
      throw new ProgrammingError("The error message must not be null");
    }

    return new TplSolution(errorMsg);
  }

  private TplSolution(TplSubstitution subst, String errorMsg, SolutionVariant variant, EnvironmentRecord env) {
    this.subst = subst;
    this.errorMsg = errorMsg;
    this.variant = variant;
    this.env = env;
  }

  private TplSolution(TplSubstitution subst, EnvironmentRecord env) {
    this(subst, null, SolutionVariant.SOLUTION,env);
  }

  private TplSolution(EnvironmentRecord env) {
    this(null, null, SolutionVariant.NO_SOLUTION,env);
  }

  private TplSolution(String errorMsg) {
    this(null, errorMsg, SolutionVariant.ERROR, null);
  }

  public boolean isSolution() {
    return variant == SolutionVariant.SOLUTION && subst != null;
  }

  public boolean isNoSolution() {
    return variant == SolutionVariant.NO_SOLUTION;
  }

  public boolean isError() {
    return (variant == SolutionVariant.ERROR || variant == SolutionVariant.ERROR_DEPTH_LIMIT_EXCEEDED)
            && errorMsg != null;
  }

  public boolean isDepthLimitExceededError() {
    return isError() && (variant == SolutionVariant.ERROR_DEPTH_LIMIT_EXCEEDED);
  }

  public TplSubstitution getSolution() {
    if (!isSolution()) {
      throw new ProgrammingError("The object does not represent a solution");
    }

    return subst;
  }

  public String getErrorMessage() {
    if (!isError()) {
      throw new ProgrammingError("The object does not represent an error");
    }

    return errorMsg;
  }

  public EnvironmentRecord getEnvironmentRecord(){
    return env;
  }
}

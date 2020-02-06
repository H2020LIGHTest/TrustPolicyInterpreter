package eu.lightest.horn.codegen;

import eu.lightest.horn.AST.*;
import eu.lightest.horn.HornBaseListener;
import eu.lightest.horn.HornParser;
import eu.lightest.horn.error.ErrorHandler;
import eu.lightest.horn.exceptions.TypeCheckingException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Walker extends HornBaseListener {

  private static Logger logger = Logger.getLogger(Walker.class);

  private boolean mInProgram = false;
  private boolean mInClause = false;
  private boolean mInFact = false;
  private boolean mInParameter = false;
  private boolean mInBody = false;
  private boolean mInTerm = false;

  private TplPolicy mTplPolicy = null;
  private TplClause mClause = null;
  private TplExpression mFact = null;
  private TplTerm mTerm = null;

  private List<TplTerm> mTermStack = new ArrayList<>();
  private List<TplTermVar> mExistingVars = new ArrayList<>();
  private boolean mInArithm;


  public TplPolicy getProgram() {
    return mTplPolicy;
  }

  @Override
  public void enterProgram(HornParser.ProgramContext ctx) {
    mInProgram = true;
    mTplPolicy = new TplPolicy();
  }

  @Override
  public void exitProgram(HornParser.ProgramContext ctx) {
    mInProgram = false;
  }


  @Override
  public void enterClause(HornParser.ClauseContext ctx) {
    mInClause = true;

    mExistingVars.clear();
    mClause = new TplClause();
    mTplPolicy.mPossibleClauses.add(mClause);
  }

  @Override
  public void exitClause(HornParser.ClauseContext ctx) {
    mInClause = false;
  }

  @Override
  public void enterFact(HornParser.FactContext ctx) {
    mInFact = true;

    if (ctx.RELOP() != null) {
      TplRelop f = new TplRelop();
      f.mOp = ctx.RELOP().getText();

      mFact = f;
    }

    if (ctx.IDENTIFIER() != null) {
      TplPredication f = new TplPredication();

      f.mId = ctx.IDENTIFIER().getText();
      mFact = f;
    }

    mClause.addFact(mFact);
  }

  @Override
  public void exitFact(HornParser.FactContext ctx) {
    mInFact = false;
    mTermStack.clear();
  }

  @Override
  public void enterTerms(HornParser.TermsContext ctx) {
    if (mTerm != null && mTerm instanceof TplTermComposite) {
      mTermStack.add(mTerm);
      mTerm = null;
      logger.debug("StackSize: " + mTermStack.size());
    }
  }

  @Override
  public void exitTerms(HornParser.TermsContext ctx) {
    if (mTermStack.size() > 0) {
      mTerm = mTermStack.get(mTermStack.size() - 1);
      mTermStack.remove(mTermStack.size() - 1);
      logger.debug("Removed StackSize: " + mTermStack.size());
    } else {
      mTerm = null;
    }


  }

  @Override
  public void enterTerm(HornParser.TermContext ctx) {
    mInTerm = true;

    if(mInArithm)
      return;
    if (ctx.ARITH() != null) {
      mTerm = extractArithm(ctx);
    }
    if (ctx.VAR() != null) {
      mTerm = extractVar(ctx);
    }

    if (ctx.INT() != null) {
      mTerm = extractInt(ctx);
    }

    if (ctx.IDENTIFIER() != null) {
      mTerm = extractIdentifier(ctx);
    }

    if (mTerm != null) {
      if (mTermStack.size() != 0) {
        TplTermComposite t = (TplTermComposite) mTermStack.get(mTermStack.size() - 1);
        t.mTerms.add(mTerm);
      } else {
        mFact.mTerms.add(mTerm);
      }

    } else
      logger.error("ERROR: Oops! Term should have always been set!");
  }

  private TplTermComposite extractIdentifier(HornParser.TermContext ctx) {
    String s = ctx.IDENTIFIER().getText();

    if (s != null && !s.isEmpty() && s.charAt(0) == '\'') {
      s = s.substring(1,s.length()-1);
    }

    return new TplTermComposite(s);
  }

  private TplTermInteger extractInt(HornParser.TermContext ctx) {
    TplTermInteger t = new TplTermInteger();
    t.mValue = Integer.parseInt(ctx.INT().getText());

    return t;
  }

  private TplTermArith extractArithm(HornParser.TermContext ctx) {
    mInArithm = true;

    TplTermArith t = new TplTermArith();
    t.mOp = ctx.ARITH().getText();

    for (int i = 0; i < 2; i++) {
      if (ctx.term().get(i).VAR() != null)
        t.mTerms.add(extractVar(ctx.term().get(i)));
      else if (ctx.term().get(i).INT() != null)
        t.mTerms.add(extractInt(ctx.term().get(i)));
      else {
        ErrorHandler.getInstance().addTypeError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
            "Only simple Arithmetic Operations are allowed");
      }
    }

    return t;
  }

  private TplTermVar extractVar(HornParser.TermContext ctx) {
    String varName = ctx.VAR().getText();

    Optional<TplTermVar> bla = mExistingVars.stream().filter(var -> var.mId.equals(varName)).findFirst();
    if (bla.isPresent()) {

      return bla.get();
    } else {

      TplTermVar t = new TplTermVar();
      t.mId = varName;
      mExistingVars.add(t);
      return t;
    }
  }

  @Override
  public void exitTerm(HornParser.TermContext ctx) {
    mInTerm = false;

    if (ctx.ARITH() != null) {
      mInArithm = false;
    }
  }
}

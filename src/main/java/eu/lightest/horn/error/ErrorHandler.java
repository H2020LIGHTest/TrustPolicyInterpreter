package eu.lightest.horn.error;


import eu.lightest.horn.Interpreter;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
  private static Logger logger = Logger.getLogger(ErrorHandler.class);

  private static ErrorHandler ourInstance = new ErrorHandler();

  public static ErrorHandler getInstance() {
    return ourInstance;
  }

  private ErrorHandler() {
  }

  private static final int LEXER_ERROR = 0;
  private static final int SYNTAX_ERROR = 1;
  private static final int TYPE_ERROR = 2;
  private static final int TYPE_WARNING = 3;

  private ErrorFactory ourErrorFactory = new ErrorFactory();

  private List<List<HornError>> ourErrors = initErrorList(4);

  private static List<List<HornError>> initErrorList(int size) {
    List<List<HornError>> list = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      list.add(new ArrayList<HornError>());
    }

    return list;
  }

  public void reset() {
    ourErrors = initErrorList(4);
  }

  private String getError(int errorType, boolean isQuery) {

    String errorStr;

    switch (errorType) {
      case LEXER_ERROR:
        errorStr = "lexical errors: ";
        break;

      case SYNTAX_ERROR:
        errorStr = "syntax errors: ";
        break;

      case TYPE_ERROR:
        errorStr = "type errors: ";
        break;

      case TYPE_WARNING:
        errorStr = "type warnings: ";
        break;

      default:
        errorStr = "";
        return null;
    }

    String lineSeparator = System.getProperty("line.separator");

    StringBuffer out = new StringBuffer();
//    out.append(lineSeparator);
    out.append((isQuery) ? "In query:\n" : "In program:\n");
    out.append("Number of ").append(errorStr).append(ourErrors.get(errorType).size());

    int errorCnt = 0;

    for (HornError error : ourErrors.get(errorType)) {
      out.append(lineSeparator);
      out.append("  #")
          .append(++errorCnt)
          .append(": line")
          .append(error.getLine())
          .append(":")
          .append(error.getPos())
          .append(" ")
          .append(error.getMessage())
          .append(lineSeparator);
    }
    return out.toString();
  }

  public void addLexerError(int line, int pos, String msg) {
    ourErrors.get(LEXER_ERROR).add(ourErrorFactory.createLexerError(msg, line, pos));
  }

  public void addSyntaxError(int line, int pos, String msg) {
    ourErrors.get(SYNTAX_ERROR).add(ourErrorFactory.createSyntaxError(msg, line, pos));
  }

  public void addTypeError(int line, int pos, String msg) {
    ourErrors.get(TYPE_ERROR).add(ourErrorFactory.createTypeError(msg, line, pos));
  }

  public int getLexerErrorCount() {
    return ourErrors.get(LEXER_ERROR).size();
  }

  public int getSyntaxErrorCount() {
    return ourErrors.get(SYNTAX_ERROR).size();
  }

  public int getTypeErrorCount() {
    return ourErrors.get(TYPE_ERROR).size();
  }

  public void printLexerErrors(boolean isQuery) {
    logger.error(getError(LEXER_ERROR, isQuery));
  }

  public void printSyntaxErrors(boolean isQuery) {
    logger.error(getError(SYNTAX_ERROR, isQuery));
  }

  public void printTypeErrors(boolean isQuery) {
    logger.error(getError(TYPE_ERROR, isQuery));
  }

  public String getLexerErrors(boolean isQuery) {
    return getError(LEXER_ERROR, isQuery);
  }

  public String getSyntaxErrors(boolean isQuery) {
    return getError(SYNTAX_ERROR, isQuery);
  }

  public String getTypeErrors(boolean isQuery) {
    return getError(TYPE_ERROR, isQuery);
  }
}

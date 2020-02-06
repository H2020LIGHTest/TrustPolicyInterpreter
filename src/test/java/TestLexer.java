import eu.lightest.horn.HornLexer;
import eu.lightest.horn.error.ErrorHandler;
import eu.lightest.horn.lex.LexerInterface;
import eu.lightest.horn.lex.LexicalAnalyzer;
import junit.framework.Assert;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;


public class TestLexer {

  private void runTest(String pathToTpl, boolean shouldPass) {
    FileInputStream program_file = null;
    String program_txt = null;
    try {
      program_file = new FileInputStream(pathToTpl);
      program_txt = IOUtils.toString(program_file);
      System.out.println(program_txt);
    } catch (IOException e) {
      e.printStackTrace();
    }

    LexerInterface lexer = new LexicalAnalyzer();
    lexer.lexer(program_txt);

    if (ErrorHandler.getInstance().getLexerErrorCount() > 0) {
      ErrorHandler.getInstance().printLexerErrors(false);
    }
    org.junit.Assert.assertEquals(ErrorHandler.getInstance().getLexerErrorCount() == 0, shouldPass);
  }

  @Test
  public void simpleHornClause() {
    runTest("src/test/testdata/lexer_tests/simple_horn_clause.tpl", true);

  }
  @Test
  public void malformedHornClause() {
    runTest("src/test/testdata/lexer_tests/malformed_horn_clause.tpl", false);
  }
  @Test
  public void underline() {
    runTest("src/test/testdata/lexer_tests/underline.tpl", true);
  }

}

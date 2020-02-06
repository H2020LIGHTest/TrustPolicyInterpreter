import eu.lightest.horn.error.ErrorHandler;
import eu.lightest.horn.lex.LexerInterface;
import eu.lightest.horn.lex.LexicalAnalyzer;
import eu.lightest.horn.syntax.SyntaxAnalyzer;
import eu.lightest.horn.syntax.SyntaxInterface;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;


public class TestSyntax {

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
      org.junit.Assert.assertTrue(false);
    }
    org.junit.Assert.assertNotNull(lexer);

    SyntaxInterface syntax = new SyntaxAnalyzer();
    syntax.syntax(lexer.getTokenStream());

    if (ErrorHandler.getInstance().getSyntaxErrorCount() > 0) {
      ErrorHandler.getInstance().printSyntaxErrors(false);
    }

    org.junit.Assert.assertNotNull(syntax);
    org.junit.Assert.assertEquals(shouldPass, ErrorHandler.getInstance().getSyntaxErrorCount() == 0);
  }

  @Test
  public void syseb_info_test() {
    runTest("src/test/testdata/syntax_tests/seb_info_test.tpl", true);
  }

  @Test
  public void sebs_detail_definition_example() {
    runTest("src/test/testdata/syntax_tests/sebs_detail_definition_example.tpl", true);
  }

  @Test
  public void malformed_arith() {
    runTest("src/test/testdata/syntax_tests/malformed_arith.tpl", false);
  }

  @Disabled
  @Test
  public void relop_as_head() { //TODO change, but not urgent : RELOP as head possible
    runTest("src/test/testdata/syntax_tests/relop_as_head.tpl", false);
  }

}

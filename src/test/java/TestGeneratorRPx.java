import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.HornFailedException;
import eu.lightest.horn.util.AtvApiDummy;
import org.junit.Assert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Disabled
public class TestGeneratorRPx {
  private final ByteArrayOutputStream mOutContent = new ByteArrayOutputStream();
  private final PrintStream oldOut = System.out;

  private void runTestRPx(String pathToTpl, String q, String inputVariable, boolean shouldBe, AtvApiDummy api, String pathToTptp) {
    boolean oldRecordRPxTranscript = Interpreter.recordRPxTranscript;
    String oldRecordRPxTranscriptLocation = Interpreter.recordRPxTranscriptLocation;
    Interpreter.recordRPxTranscript = true;
    Interpreter.recordRPxTranscriptLocation = pathToTptp;

    System.out.println("path= " + System.getProperty("user.dir"));
    Interpreter m = new Interpreter(api);
    try {
      Assert.assertEquals(shouldBe, m.run(pathToTpl, q, inputVariable));
    } catch (HornFailedException e) {
      e.printStackTrace();
      Assert.fail();
    }

    Interpreter.recordRPxTranscript = oldRecordRPxTranscript;
    Interpreter.recordRPxTranscriptLocation = oldRecordRPxTranscriptLocation;
  }

  private void runTestRPx(String pathToTpl, String q, String inputVariable, boolean shouldBe, String pathToTptp) {
    runTestRPx(pathToTpl, q, inputVariable, shouldBe, new AtvApiDummy(), pathToTptp);
  }

  private void runOutputTestRPx(String pathToTpl, String q, String inputVariable, boolean shouldBe, String targetString, AtvApiDummy api, String pathToTptp) {
    boolean oldRecordRPxTranscript = Interpreter.recordRPxTranscript;
    String oldRecordRPxTranscriptLocation = Interpreter.recordRPxTranscriptLocation;
    Interpreter.recordRPxTranscript = true;
    Interpreter.recordRPxTranscriptLocation = pathToTptp;

    // prepare
    System.out.println("path= " + System.getProperty("user.dir"));
    System.setOut(new PrintStream(mOutContent));

    // main
    try {
      Interpreter m = new Interpreter(api);
      try {
        boolean outcome = m.run(pathToTpl, q, inputVariable);
        Assert.assertEquals(shouldBe, outcome);
        Assert.assertTrue(mOutContent.toString().contains(targetString));
      } catch (HornFailedException e) {
        System.out.println(e.getMessage());
      }
    } finally {
      //end
      System.setOut(oldOut);
      System.out.println(mOutContent);
    }

    Interpreter.recordRPxTranscript = oldRecordRPxTranscript;
    Interpreter.recordRPxTranscriptLocation = oldRecordRPxTranscriptLocation;
  }


  private boolean checkRPx(String tptp) {
    String s;
    try {
      String[] commands = {"bash", "-c", "echo \"" + tptp + "\" | gtimeout 20 rpxprover - "};
      Process p = Runtime.getRuntime().exec(commands);
      BufferedReader stdInput = new BufferedReader(new
              InputStreamReader(p.getInputStream()));

      // read the output from the command

      String out = "";
      while ((s = stdInput.readLine()) != null) {
        out+=s + "\n";
      }
      return out.contains("Unsatisfiable");
    }  catch (IOException e) {
      Assert.fail("IOException: " + e);
    }
    return false;
  }

  private void checkRPx(String pathToTptp, boolean expected) {
    String tptp = null;
    boolean actual;
    try {
      List<String> content = Files.readAllLines(Paths.get(pathToTptp), StandardCharsets.UTF_8);
      tptp = String.join("\n",content);
      tptp += "\n";
      actual = checkRPx(tptp);
    } catch (IOException e) {
      actual = false;
    }

    System.out.println(tptp);
    Assert.assertEquals(expected,actual);
  }

  private void runOutputTestRPx(String pathToTpl, String q, String inputVariable, boolean shouldBe, String outputString, String pathToTptp) {
    runOutputTestRPx(pathToTpl, q, inputVariable, shouldBe, outputString, new AtvApiDummy(), pathToTptp);
  }


@Test
  public void rpx_simple_trust_a() {
    runTestRPx("src/test/testdata/generator_tests/simple_trust.tpl"
            , "trust(a).", "Input", true, "src/test/targets/rpx_simple_trust_a.tptp");
    checkRPx("src/test/targets/rpx_simple_trust_a.tptp", true);

  }

@Test
  public void rpx_simple_trust_a_query_not() {
    runTestRPx("src/test/testdata/generator_tests/simple_trust.tpl"
            , "trust(not(a)).", "Input", false, "src/test/targets/rpx_simple_trust_a_query_not.tptp");

    checkRPx("src/test/targets/rpx_simple_trust_a_query_not.tptp",false);
  }

  @Test
  public void rpx_simple_trust_XA() {
    runTestRPx("src/test/testdata/generator_tests/simple_trust_x.tpl"
            , "trust(a).", "Input", true, "src/test/targets/rpx_simple_trust_XA.tptp");

    checkRPx("src/test/targets/rpx_simple_trust_XA.tptp",true);
  }

@Test
  public void rpx_simple_trust_X_2() {
    runTestRPx("src/test/testdata/generator_tests/simple_trust_x.tpl"
            , "trust(X).", "Input", true, "src/test/targets/rpx_simple_trust_X_2.tptp");

    checkRPx("src/test/targets/rpx_simple_trust_X_2.tptp",true);
  }

@Test
  public void rpx_TrustDelegationProgramSimpleFindTrust() {
    runTestRPx("src/test/testdata/generator_tests/trust_delegation.tpl"
            , "trust(a).", "Input", true, "src/test/targets/rpx_TrustDelegationProgramSimpleFindTrust.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgramSimpleFindTrust.tptp",true);
  }

@Test
  public void rpx_TrustDelegationProgram() {
    runTestRPx("src/test/testdata/generator_tests/trust_delegation.tpl"
            , "trust(c).", "Input", true,"src/test/targets/rpx_TrustDelegationProgram.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgram.tptp",true);
  }
@Test
  public void rpx_only_entities() {
    runTestRPx("src/test/testdata/generator_tests/only_entities.tpl"
            , "trust(a).", "Input", true, "src/test/targets/rpx_only_entities.tptp");

    checkRPx("src/test/targets/rpx_only_entities.tptp",true);
  }

@Test
  public void rpx_trust_delegation_wrong_then_right_facts() {
    runTestRPx("src/test/testdata/generator_tests/two_possible_facts_correct.tpl"
            , "trust(c).", "Input", true, "src/test/targets/rpx_trust_delegation_wrong_then_right_facts.tptp");

    checkRPx("src/test/targets/rpx_trust_delegation_wrong_then_right_facts.tptp",true);
  }

@Test
  public void rpx_trust_delegation_wrong_then_wrong_facts() {
    runTestRPx("src/test/testdata/generator_tests/two_possible_facts_wrong.tpl"
            , "trust(c).", "Input", false, "src/test/targets/rpx_trust_delegation_wrong_then_wrong_facts.tptp");

    checkRPx("src/test/targets/rpx_trust_delegation_wrong_then_wrong_facts.tptp",false);
  }

@Test
  public void rpx_two_possible_rules() {
    runTestRPx("src/test/testdata/generator_tests/two_possible_rules.tpl"
            , "trust(c).", "Input", true, "src/test/targets/rpx_two_possible_rules.tptp");

    checkRPx("src/test/targets/rpx_two_possible_rules.tptp",true);
  }

@Test
  public void rpx_TrustDelegationProgram_half() {
    runTestRPx("src/test/testdata/generator_tests/trust_delegation.tpl"
            , "trust(b).", "Input", true, "src/test/targets/rpx_TrustDelegationProgram_half.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgram_half.tptp",true);
  }

@Test
  public void rpx_TrustDelegationProgram_special01() {
    runTestRPx("src/test/testdata/generator_tests/trust_delegation_special01.tpl"
            , "trust(c).", "Input", true, "src/test/targets/rpx_TrustDelegationProgram_special01.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgram_special01.tptp",true);
  }

@Test
  public void rpx_TrustDelegationProgram_special02() {
    runTestRPx("src/test/testdata/generator_tests/trust_delegation_special02.tpl"
            , "trust(c).", "Input", true, "src/test/targets/rpx_TrustDelegationProgram_special02.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgram_special02.tptp",true);
  }

@Test
  public void rpx_TrustDelegationProgram_doubleTrust() {
    runTestRPx("src/test/testdata/generator_tests/trust_delegation_doubleTrust.tpl"
            , "trust(e).", "Input", true, "src/test/targets/rpx_TrustDelegationProgram_doubleTrust.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgram_doubleTrust.tptp",true);
  }

@Test
  public void rpx_TrustDelegationProgramKasWurst() {
    runTestRPx("src/test/testdata/generator_tests/trust_delegation_kasWurst.tpl"
            , "trust(c).", "Input", true, "src/test/targets/rpx_TrustDelegationProgramKasWurst.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgramKasWurst.tptp",true);
  }

@Test
  public void rpx_sebs_intro_prolog_test01() {
    runTestRPx("src/test/testdata/generator_tests/sebs_intro_prolog_test.tpl"
            , "trust(e, 2).", "Input", true, "src/test/targets/rpx_sebs_intro_prolog_test01.tptp");

    checkRPx("src/test/targets/rpx_sebs_intro_prolog_test01.tptp",true);
  }

@Test
  public void rpx_sebs_intro_prolog_test02() {
    runTestRPx("src/test/testdata/generator_tests/sebs_intro_prolog_test.tpl"
            , "trust(e, 1).", "Input", false, "src/test/targets/rpx_sebs_intro_prolog_test02.tptp");

    checkRPx("src/test/targets/rpx_sebs_intro_prolog_test02.tptp",false);
  }

@Test
  public void rpx_sebs_detail_definition_example01() {
    runTestRPx("src/test/testdata/generator_tests/sebs_detail_definition_example.tpl"
            , "trust(a).", "Input", true, "src/test/targets/rpx_sebs_detail_definition_example01.tptp");

    checkRPx("src/test/targets/rpx_sebs_detail_definition_example01.tptp",true);
  }

@Test
  public void rpx_sebs_detail_definition_example02() {
    runTestRPx("src/test/testdata/generator_tests/sebs_detail_definition_example.tpl"
            , "trust(b).", "Input", false, "src/test/targets/rpx_sebs_detail_definition_example02.tptp");

    checkRPx("src/test/targets/rpx_sebs_detail_definition_example02.tptp",false);
  }

@Test
  public void rpx_circular_inf_loop() {
    runOutputTestRPx("src/test/testdata/generator_tests/simple_circular.tpl"
            , "trust(d).", "Input", false, "Error: The Tpl has an infinity loop!", "src/test/targets/rpx_circular_inf_loop.tptp");

    checkRPx("src/test/targets/rpx_circular_inf_loop.tptp",false);
  }

@Test
  public void rpx_avr_without_value() {
    runTestRPx("src/test/testdata/generator_tests/avr_without_value.tpl"
            , "trust(d).", "Input", false /*, "Warning: Variable without any chance of getting a Value!"*/, "src/test/targets/rpx_avr_without_value.tptp");

    checkRPx("src/test/targets/rpx_avr_without_value.tptp",false);
  }

  /**
   * keyword plugin system tests ----------------------------------------------------------------
   */

@Test
  public void rpx_simple_print() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_print.tpl"
            , "main(Input).", "Input", true, "src/test/targets/rpx_simple_print.tptp");

    checkRPx("src/test/targets/rpx_simple_print.tptp",true);
  }

@Test
  public void rpx_simple_extract() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_extract_test.tpl"
            , "main(Input).", "Input", true, "src/test/targets/rpx_simple_extract.tptp");

    checkRPx("src/test/targets/rpx_simple_extract.tptp",true);
  }

@Test
  public void rpx_simple_extract_with_const() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_extract_test_const.tpl"
            , "main(Input).", "Input", true,
            new AtvApiDummy()
                    .addDocumentEntry("theAuctionHouse2018format","input","format"),"src/test/targets/rpx_simple_extract_with_const.tptp");

    checkRPx("src/test/targets/rpx_simple_extract_with_const.tptp",true);
  }

@Test
  public void rpx_simple_verify_signaure() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_verify_signaure.tpl"
            , "main(Input).", "Input", true, "src/test/targets/rpx_simple_verify_signaure.tptp");

    checkRPx("src/test/targets/rpx_simple_verify_signaure.tptp",true);
  }

@Test
  public void rpx_simple_trustscheme() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_trustscheme.tpl"
            , "main(Input).", "Input", true, "src/test/targets/rpx_simple_trustscheme.tptp");

    checkRPx("src/test/targets/rpx_simple_trustscheme.tptp",true);
  }

@Test
  public void rpx_simple_lookup_test() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_lookup_test.tpl"
            , "main(Input).", "Input", true, "src/test/targets/rpx_simple_lookup_test.tptp");

    checkRPx("src/test/targets/rpx_simple_lookup_test.tptp",true);
  }

@Test
  public void rpx_simple_wrong_extract_test01() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_extract_test01.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_extract_test01.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_extract_test01.tptp",false);
  }


@Test
  public void rpx_simple_wrong_extract_test03() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_extract_test03.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_extract_test03.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_extract_test03.tptp",false);
  }

@Test
  public void rpx_simple_wrong_lookup_test01() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_lookup_test01.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_lookup_test01.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_lookup_test01.tptp",false);
  }

@Test
  public void rpx_simple_wrong_lookup_test02() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_lookup_test02.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_lookup_test02.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_lookup_test02.tptp",false);
  }

@Test
  public void rpx_simple_print02() {
    runOutputTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_print02.tpl"
            , "main(Input).", "Input", true, "Horn says: input","src/test/targets/rpx_simple_print02.tptp");
    checkRPx("src/test/targets/rpx_simple_print02.tptp",true);
  }

@Test
  public void rpx_simple_print03() {
    runOutputTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_print03.tpl"
            , "main(Input).", "Input", true, "Horn says: [input]", "src/test/targets/rpx_simple_print03.tptp");
    checkRPx("src/test/targets/rpx_simple_print03.tptp",true);
  }

@Test
  public void rpx_simple_wrong_trustscheme01() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_trustscheme01.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_trustscheme01.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_trustscheme01.tptp",false);
  }

@Test
  public void rpx_simple_wrong_trustscheme02() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_trustscheme02.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_trustscheme02.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_trustscheme02.tptp",false);
  }

@Test
  public void rpx_simple_wrong_verify_signaure01() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_verify_signaure01.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_verify_signaure01.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_verify_signaure01.tptp",false);
  }

@Test
  public void rpx_simple_wrong_verify_signaure02() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_verify_signaure02.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_verify_signaure02.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_verify_signaure02.tptp",false);
  }

@Test
  public void rpx_paper_policy_specification_01() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/paper_policy_specification_01.tpl"
            , "accept(Input).", "Input", true,
            new AtvApiDummy()
                    .addDocumentEntry("theAuctionHouse2019Format", "input", "format")
                    .addDocumentEntry(99, "input", "bid")
    , "src/test/targets/rpx_paper_policy_specification_01.tptp");

    checkRPx("src/test/targets/rpx_paper_policy_specification_01.tptp",true);
  }

@Test
  public void rpx_paper_policy_specification_02() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/paper_policy_specification_02.tpl"
            , "accept(Input).", "Input", true,
            new AtvApiDummy()
                    .addDocumentEntry("theAuctionHouse2019format", "input", "format")
                    .addDocumentEntry(8, "input", "bid")
                    .addDocumentEntry("eIDAS_qualified_certificate", "input", "certificate", "format")
                    .addDocumentEntry("eIDAS_qualified", "input", "certificate", "issuer", "trustScheme")
    , "src/test/targets/rpx_paper_policy_specification_02.tptp");

    checkRPx("src/test/targets/rpx_paper_policy_specification_02.tptp",true);
  }

@Test
  public void rpx_paper_policy_specification_03() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/paper_policy_specification_03.tpl"
            , "accept(Input).", "Input", true,
            new AtvApiDummy()
                    .addDocumentEntry(8, "input", "document", "bid")
                    .addDocumentEntry("delegation", "input", "mandate", "format")
                    .addDocumentEntry("place_bid", "input", "mandate", "purpose")
    , "src/test/targets/rpx_paper_policy_specification_03.tptp");

    checkRPx("src/test/targets/rpx_paper_policy_specification_03.tptp",true);
  }

@Test
  public void rpx_recursion_problem_test() { //FIXME
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/recursion_problem_test.tpl"
            , "accept(Input).", "Input", false, "src/test/targets/rpx_recursion_problem_test.tptp");

    checkRPx("src/test/targets/rpx_recursion_problem_test.tptp",false);
  }

@Test
  public void rpx_simple_contract() {
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_contract.tpl"
            , "accept(Form).", "Form", true,
            new AtvApiDummy()
                    .addDocumentEntry("simpleContract","input","format"), "src/test/targets/rpx_simple_contract.tptp");

    checkRPx("src/test/targets/rpx_simple_contract.tptp",true);
  }

@Test
  public void rpx_id() {
    runTestRPx("src/test/testdata/generator_tests/id.tpl"
            , "id(c,c).", "Form", true, "src/test/targets/rpx_id.tptp");

    checkRPx("src/test/targets/rpx_id.tptp",true);
  }

@Test
  public void rpx_id2() {
    runTestRPx("src/test/testdata/generator_tests/id.tpl"
            , "id(X,Z).", "Form", true, "src/test/targets/rpx_id2.tptp");

    checkRPx("src/test/targets/rpx_id2.tptp",true);
  }

@Test
  public void rpx_id3() {
    runTestRPx("src/test/testdata/generator_tests/id.tpl"
            , "id(c,d).", "Form", false, "src/test/targets/rpx_id3.tptp");

    checkRPx("src/test/targets/rpx_id3.tptp",false);
  }

@Test
  public void rpx_id4() {
    runTestRPx("src/test/testdata/generator_tests/id.tpl"
            , "id(f(Y,g(Z),Y), f(f(U),g(c),Y)).", "Form", true, "src/test/targets/rpx_id4.tptp");

    checkRPx("src/test/targets/rpx_id4.tptp",true);
  }

@Test
  public void rpx_compositeTerm() {
    runTestRPx("src/test/testdata/generator_tests/composite_term.tpl"
            , "ftrust(a).", "Form", true, "src/test/targets/rpx_compositeTerm.tptp");

    checkRPx("src/test/targets/rpx_compositeTerm.tptp",true);
  }

@Test
  public void rpx_list() {
    runTestRPx("src/test/testdata/generator_tests/list.tpl"
            , "isMember(b,cons(a,cons(b,cons(c,empty)))).", "Form", true, "src/test/targets/rpx_list.tptp");

    checkRPx("src/test/targets/rpx_list.tptp",true);
  }

@Test
  public void rpx_list2() {
    runTestRPx("src/test/testdata/generator_tests/list.tpl"
            , "isMember(d,cons(a,cons(b,cons(c,empty)))).", "Form", false, "src/test/targets/rpx_list2.tptp");

    checkRPx("src/test/targets/rpx_list2.tptp",false);
  }

@Test
  public void rpx_list3() {
    runTestRPx("src/test/testdata/generator_tests/list.tpl"
            , "test().", "Form", true, "src/test/targets/rpx_list3.tptp");

    checkRPx("src/test/targets/rpx_list3.tptp",true);
  }

@Test
  public void rpx_list4() {
    runTestRPx("src/test/testdata/generator_tests/list.tpl"
            , "failtest().", "Form", false, "src/test/targets/rpx_list4.tptp");

    checkRPx("src/test/targets/rpx_list4.tptp",false);
  }
}

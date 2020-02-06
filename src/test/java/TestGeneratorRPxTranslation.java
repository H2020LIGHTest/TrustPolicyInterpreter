import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.HornFailedException;
import eu.lightest.horn.util.AtvApiDummy;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TestGeneratorRPxTranslation {
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

  private void checkRPx(String pathToTptp, String expected) {
    String tptp;
    try {
      List<String> content = Files.readAllLines(Paths.get(pathToTptp), StandardCharsets.UTF_8);
      tptp = String.join("\n",content);
      tptp += "\n";
    } catch (IOException e) {
      tptp = null;
    }
    Assert.assertEquals(expected,tptp);
  }

  private void runOutputTestRPx(String pathToTpl, String q, String inputVariable, boolean shouldBe, String outputString, String pathToTptp) {
    runOutputTestRPx(pathToTpl, q, inputVariable, shouldBe, outputString, new AtvApiDummy(), pathToTptp);
  }


  @Test
  public void rpx_simple_trust_a() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_a_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/simple_trust.tpl"
            , "trust(a).", "Input", true, "src/test/targets/rpx_simple_trust_a.tptp");
    checkRPx("src/test/targets/rpx_simple_trust_a.tptp", expected);

  }

  @Test
  public void rpx_simple_trust_a_query_not() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_not_1(term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/simple_trust.tpl"
            , "trust(not(a)).", "Input", false, "src/test/targets/rpx_simple_trust_a_query_not.tptp");

    checkRPx("src/test/targets/rpx_simple_trust_a_query_not.tptp", expected);
  }

  @Test
  public void rpx_simple_trust_XA() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_a_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X))).\n";
    runTestRPx("src/test/testdata/generator_tests/simple_trust_x.tpl"
            , "trust(a).", "Input", true, "src/test/targets/rpx_simple_trust_XA.tptp");

    checkRPx("src/test/targets/rpx_simple_trust_XA.tptp",expected);
  }

  @Test
  public void rpx_simple_trust_X_2() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(Var_X)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X))).\n";
    runTestRPx("src/test/testdata/generator_tests/simple_trust_x.tpl"
            , "trust(X).", "Input", true, "src/test/targets/rpx_simple_trust_X_2.tptp");

    checkRPx("src/test/targets/rpx_simple_trust_X_2.tptp",expected);
  }

  @Test
  public void rpx_TrustDelegationProgramSimpleFindTrust() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_a_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_b_0, term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_delegate_2(Var_X, Var_Y)| ~pred_trust_1(Var_Y))).\n";
    runTestRPx("src/test/testdata/generator_tests/trust_delegation.tpl"
            , "trust(a).", "Input", true, "src/test/targets/rpx_TrustDelegationProgramSimpleFindTrust.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgramSimpleFindTrust.tptp",expected);
  }

  @Test
  public void rpx_TrustDelegationProgram() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_c_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_b_0, term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_delegate_2(Var_X, Var_Y)| ~pred_trust_1(Var_Y))).\n";
    runTestRPx("src/test/testdata/generator_tests/trust_delegation.tpl"
            , "trust(c).", "Input", true,"src/test/targets/rpx_TrustDelegationProgram.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgram.tptp",expected);
  }
  @Test
  public void rpx_only_entities() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_a_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_a_0, term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0)| ~pred_delegate_2(term_a_0, term_b_0)| ~pred_trust_1(term_b_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/only_entities.tpl"
            , "trust(a).", "Input", true, "src/test/targets/rpx_only_entities.tptp");

    checkRPx("src/test/targets/rpx_only_entities.tptp", expected);
  }

  @Test
  public void rpx_trust_delegation_wrong_then_right_facts() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_c_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_f_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_b_0, term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_e_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_e_0, term_f_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_delegate_2(Var_X, Var_Y)| ~pred_trust_1(Var_Y))).\n";
    runTestRPx("src/test/testdata/generator_tests/two_possible_facts_correct.tpl"
            , "trust(c).", "Input", true, "src/test/targets/rpx_trust_delegation_wrong_then_right_facts.tptp");

    checkRPx("src/test/targets/rpx_trust_delegation_wrong_then_right_facts.tptp",expected);
  }

  @Test
  public void rpx_trust_delegation_wrong_then_wrong_facts() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_c_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_z_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_b_0, term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_e_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_e_0, term_f_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_delegate_2(Var_X, Var_Y)| ~pred_trust_1(Var_Y))).\n";
    runTestRPx("src/test/testdata/generator_tests/two_possible_facts_wrong.tpl"
            , "trust(c).", "Input", false, "src/test/targets/rpx_trust_delegation_wrong_then_wrong_facts.tptp");

    checkRPx("src/test/targets/rpx_trust_delegation_wrong_then_wrong_facts.tptp", expected);
  }

  @Test
  public void rpx_two_possible_rules() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_c_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_f_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_b_0, term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_bla_2(term_c_0, term_e_0))).\n" +
            "cnf(tpl,axiom,(pred_bla_2(term_e_0, term_f_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_delegate_2(Var_X, Var_Y)| ~pred_trust_1(Var_Y))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_bla_2(Var_X, Var_Y)| ~pred_trust_1(Var_Y))).\n";
    runTestRPx("src/test/testdata/generator_tests/two_possible_rules.tpl"
            , "trust(c).", "Input", true, "src/test/targets/rpx_two_possible_rules.tptp");

    checkRPx("src/test/targets/rpx_two_possible_rules.tptp",expected);
  }

  @Test
  public void rpx_TrustDelegationProgram_half() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_b_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_b_0, term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_delegate_2(Var_X, Var_Y)| ~pred_trust_1(Var_Y))).\n";
    runTestRPx("src/test/testdata/generator_tests/trust_delegation.tpl"
            , "trust(b).", "Input", true, "src/test/targets/rpx_TrustDelegationProgram_half.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgram_half.tptp",expected);
  }

  @Test
  public void rpx_TrustDelegationProgram_special01() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_c_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_b_0, term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_u_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_delegate_2(Var_X, Var_Y)| ~pred_trust_1(Var_Y))).\n";
    runTestRPx("src/test/testdata/generator_tests/trust_delegation_special01.tpl"
            , "trust(c).", "Input", true, "src/test/targets/rpx_TrustDelegationProgram_special01.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgram_special01.tptp",expected);
  }

  @Test
  public void rpx_TrustDelegationProgram_special02() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_c_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_b_0, term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_delegate_2(Var_X, Var_Y)| ~pred_trust_1(Var_Y))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_trust_1(Var_Y)| ~pred_delegate_2(Var_X, Var_Y))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_trust_1(Var_Y)| ~pred_delegate_2(Var_Y, Var_Y))).\n";
    runTestRPx("src/test/testdata/generator_tests/trust_delegation_special02.tpl"
            , "trust(c).", "Input", true, "src/test/targets/rpx_TrustDelegationProgram_special02.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgram_special02.tptp",expected);
  }

  @Test
  public void rpx_TrustDelegationProgram_doubleTrust() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_e_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_c_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_b_0, term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_d_0, term_c_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_e_0, term_d_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_delegate_2(Var_X, Var_Y)| ~pred_trust_1(Var_Y))).\n";
    runTestRPx("src/test/testdata/generator_tests/trust_delegation_doubleTrust.tpl"
            , "trust(e).", "Input", true, "src/test/targets/rpx_TrustDelegationProgram_doubleTrust.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgram_doubleTrust.tptp",expected);
  }

  @Test
  public void rpx_TrustDelegationProgramKasWurst() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_c_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_kaswurst_2(term_b_0, term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_kaswurst_2(term_c_0, term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_kaswurst_2(Var_X, Var_Y)| ~pred_trust_1(Var_Y))).\n";
    runTestRPx("src/test/testdata/generator_tests/trust_delegation_kasWurst.tpl"
            , "trust(c).", "Input", true, "src/test/targets/rpx_TrustDelegationProgramKasWurst.tptp");

    checkRPx("src/test/targets/rpx_TrustDelegationProgramKasWurst.tptp", expected);
  }

  @Test
  public void rpx_sebs_intro_prolog_test01() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_2(term_e_0, term_2_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_c_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_d_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_d_0, term_e_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_2(Var_X, Var_N)| ~pred_trust_1(Var_X))).\n" +
            "cnf(tpl,axiom,(pred_trust_2(Var_X, Var_N)| ~op_gr(Var_N, term_0_0)| ~pred_delegate_2(Var_Y, Var_X)| ~pred_trust_2(Var_Y, arith_minus(Var_N,term_1_0)))).\n" +
            "cnf(tpl,axiom,(op_gr(term_2_0, term_0_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_2(term_d_0, arith_minus(term_2_0,term_1_0))| ~pred_trust_2(term_d_0, term_1_0))).\n" +
            "cnf(tpl,axiom,(op_gr(term_1_0, term_0_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_2(term_c_0, arith_minus(term_1_0,term_1_0))| ~pred_trust_2(term_c_0, term_0_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/sebs_intro_prolog_test.tpl"
            , "trust(e, 2).", "Input", true, "src/test/targets/rpx_sebs_intro_prolog_test01.tptp");

    checkRPx("src/test/targets/rpx_sebs_intro_prolog_test01.tptp", expected);
  }

  @Test
  public void rpx_sebs_intro_prolog_test02() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_2(term_e_0, term_1_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_c_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_d_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_d_0, term_e_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_2(Var_X, Var_N)| ~pred_trust_1(Var_X))).\n" +
            "cnf(tpl,axiom,(pred_trust_2(Var_X, Var_N)| ~op_gr(Var_N, term_0_0)| ~pred_delegate_2(Var_Y, Var_X)| ~pred_trust_2(Var_Y, arith_minus(Var_N,term_1_0)))).\n";
    runTestRPx("src/test/testdata/generator_tests/sebs_intro_prolog_test.tpl"
            , "trust(e, 1).", "Input", false, "src/test/targets/rpx_sebs_intro_prolog_test02.tptp");

    checkRPx("src/test/targets/rpx_sebs_intro_prolog_test02.tptp",expected);
  }

  @Test
  public void rpx_sebs_detail_definition_example01() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_a_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_loa_2(Var_X, Var_L)| ~op_gr(Var_L, term_1_0))).\n" +
            "cnf(tpl,axiom,(pred_loa_2(Var_X, term_3_0)| ~pred_quality_2(Var_X, term_good_0))).\n" +
            "cnf(tpl,axiom,(pred_loa_2(term_a_0, term_2_0))).\n" +
            "cnf(tpl,axiom,(pred_loa_2(term_b_0, term_1_0))).\n" +
            "cnf(tpl,axiom,(pred_quality_2(term_c_0, term_good_0))).\n" +
            "cnf(tpl,axiom,(op_gr(term_2_0, term_1_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/sebs_detail_definition_example.tpl"
            , "trust(a).", "Input", true, "src/test/targets/rpx_sebs_detail_definition_example01.tptp");

    checkRPx("src/test/targets/rpx_sebs_detail_definition_example01.tptp", expected);
  }

  @Test
  public void rpx_sebs_detail_definition_example02() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_b_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~pred_loa_2(Var_X, Var_L)| ~op_gr(Var_L, term_1_0))).\n" +
            "cnf(tpl,axiom,(pred_loa_2(Var_X, term_3_0)| ~pred_quality_2(Var_X, term_good_0))).\n" +
            "cnf(tpl,axiom,(pred_loa_2(term_a_0, term_2_0))).\n" +
            "cnf(tpl,axiom,(pred_loa_2(term_b_0, term_1_0))).\n" +
            "cnf(tpl,axiom,(pred_quality_2(term_c_0, term_good_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/sebs_detail_definition_example.tpl"
            , "trust(b).", "Input", false, "src/test/targets/rpx_sebs_detail_definition_example02.tptp");

    checkRPx("src/test/targets/rpx_sebs_detail_definition_example02.tptp", expected);
  }

  @Test
  public void rpx_circular_inf_loop() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n";
    runOutputTestRPx("src/test/testdata/generator_tests/simple_circular.tpl"
            , "trust(d).", "Input", false, "Error: The Tpl has an infinity loop!", "src/test/targets/rpx_circular_inf_loop.tptp");

    checkRPx("src/test/targets/rpx_circular_inf_loop.tptp",expected);
  }

  @Test
  public void rpx_avr_without_value() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_trust_1(term_d_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_a_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_b_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_c_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_c_0, term_d_0))).\n" +
            "cnf(tpl,axiom,(pred_delegate_2(term_d_0, term_e_0))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(Var_X)| ~op_gr(Var_N, term_0_0)| ~pred_delegate_2(Var_Y, Var_X)| ~pred_trust_2(Var_Y, arith_minus(Var_N,term_1_0)))).\n";
    runTestRPx("src/test/testdata/generator_tests/avr_without_value.tpl"
            , "trust(d).", "Input", false /*, "Warning: Variable without any chance of getting a Value!"*/, "src/test/targets/rpx_avr_without_value.tptp");

    checkRPx("src/test/targets/rpx_avr_without_value.tptp",expected);
  }

  /**
   * keyword plugin system tests ----------------------------------------------------------------
   */

  @Test
  public void rpx_simple_print() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_print_1(term_3_0))).\n" +
            "cnf(tpl,axiom,(pred_print_1(term_3_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_print.tpl"
            , "main(Input).", "Input", true, "src/test/targets/rpx_simple_print.tptp");

    checkRPx("src/test/targets/rpx_simple_print.tptp",expected);
  }

  @Test
  public void rpx_simple_extract() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, term_transaction_0, Var_Transaction)| ~pred_print_1(Var_Transaction))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_transaction_0, obj_input_transaction))).\n" +
            "cnf(tpl,axiom,(pred_print_1(obj_input_transaction))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_extract_test.tpl"
            , "main(Input).", "Input", true, "src/test/targets/rpx_simple_extract.tptp");

    checkRPx("src/test/targets/rpx_simple_extract.tptp",expected);
  }

  @Test
  public void rpx_simple_extract_with_const() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, term_format_0, term_theAuctionHouse2018format_0)| ~pred_print_1(Var_Transaction))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_format_0, term_theAuctionHouse2018format_0))).\n" +
            "cnf(tpl,axiom,(pred_print_1(Var_Transaction))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_extract_test_const.tpl"
            , "main(Input).", "Input", true,
            new AtvApiDummy()
                    .addDocumentEntry("theAuctionHouse2018format","input","format"),"src/test/targets/rpx_simple_extract_with_const.tptp");

    checkRPx("src/test/targets/rpx_simple_extract_with_const.tptp",expected);
  }

  @Test
  public void rpx_simple_verify_signaure() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, term_transaction_0, Var_Transaction)| ~pred_extract_3(Var_Transaction, term_document_0, Var_Document)| ~pred_extract_3(Var_Transaction, term_issuerKey_0, Var_PkIssuer)| ~pred_verify_signature_2(Var_Document, Var_PkIssuer))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_transaction_0, obj_input_transaction))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_transaction, term_document_0, obj_input_transaction_document))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_transaction, term_issuerKey_0, obj_input_transaction_issuerKey))).\n" +
            "cnf(tpl,axiom,(pred_verify_signature_2(obj_input_transaction_document, obj_input_transaction_issuerKey))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_verify_signaure.tpl"
            , "main(Input).", "Input", true, "src/test/targets/rpx_simple_verify_signaure.tptp");

    checkRPx("src/test/targets/rpx_simple_verify_signaure.tptp",expected);
  }

  @Test
  public void rpx_simple_trustscheme() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, term_transaction_0, Var_Transaction)| ~pred_extract_3(Var_Transaction, term_trustList_0, Var_Claim)| ~pred_trustscheme_2(Var_Claim, term_eIDAS_qualified_0))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_transaction_0, obj_input_transaction))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_transaction, term_trustList_0, obj_input_transaction_trustList))).\n" +
            "cnf(tpl,axiom,(pred_trustscheme_2(obj_input_transaction_trustList, term_eIDAS_qualified_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_trustscheme.tpl"
            , "main(Input).", "Input", true, "src/test/targets/rpx_simple_trustscheme.tptp");

    checkRPx("src/test/targets/rpx_simple_trustscheme.tptp",expected);
  }

  @Test
  public void rpx_simple_lookup_test() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, term_transaction_0, Var_Transaction)| ~pred_extract_3(Var_Transaction, term_trustList_0, Var_Claim)| ~pred_lookup_2(Var_Claim, Var_TrustListEntry))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_transaction_0, obj_input_transaction))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_transaction, term_trustList_0, obj_input_transaction_trustList))).\n" +
            "cnf(tpl,axiom,(pred_lookup_2(obj_input_transaction_trustList, obj_))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_lookup_test.tpl"
            , "main(Input).", "Input", true, "src/test/targets/rpx_simple_lookup_test.tptp");

    checkRPx("src/test/targets/rpx_simple_lookup_test.tptp",expected);
  }

  @Test
  public void rpx_simple_wrong_extract_test01() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(term_input_0, term_transaction_0, Var_Transaction)| ~pred_print_1(Var_Transaction))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_extract_test01.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_extract_test01.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_extract_test01.tptp", expected);
  }


  @Test
  public void rpx_simple_wrong_extract_test03() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, Var_Transaction, Var_Transaction)| ~pred_print_1(Var_Transaction))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_extract_test03.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_extract_test03.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_extract_test03.tptp", expected);
  }

  @Test
  public void rpx_simple_wrong_lookup_test01() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, term_transaction_0, Var_Transaction)| ~pred_extract_3(Var_Transaction, term_trustList_0, Var_Claim)| ~pred_lookup_2(Var_Claim, term_trustListEntry_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_lookup_test01.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_lookup_test01.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_lookup_test01.tptp", expected);
  }

  @Test
  public void rpx_simple_wrong_lookup_test02() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, term_transaction_0, Var_Transaction)| ~pred_extract_3(Var_Transaction, term_trustList_0, Var_Claim)| ~pred_lookup_2(term_claim_0, Var_TrustListEntry))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_lookup_test02.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_lookup_test02.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_lookup_test02.tptp", expected);
  }

  @Test
  public void rpx_simple_print02() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_print_1(term_input_0))).\n" +
            "cnf(tpl,axiom,(pred_print_1(term_input_0))).\n";
    runOutputTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_print02.tpl"
            , "main(Input).", "Input", true, "Horn says: input","src/test/targets/rpx_simple_print02.tptp");
    checkRPx("src/test/targets/rpx_simple_print02.tptp",expected);
  }

  @Test
  public void rpx_simple_print03() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_print_1(Var_Input))).\n" +
            "cnf(tpl,axiom,(pred_print_1(obj_input))).\n";
    runOutputTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_print03.tpl"
            , "main(Input).", "Input", true, "Horn says: [input]", "src/test/targets/rpx_simple_print03.tptp");
    checkRPx("src/test/targets/rpx_simple_print03.tptp", expected);
  }

  @Test
  public void rpx_simple_wrong_trustscheme01() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, term_transaction_0, Var_Transaction)| ~pred_extract_3(Var_Transaction, term_trustList_0, Var_Claim)| ~pred_trustscheme_2(Var_Claim, Var_IDAS))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_trustscheme01.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_trustscheme01.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_trustscheme01.tptp", expected);
  }

  @Test
  public void rpx_simple_wrong_trustscheme02() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, term_transaction_0, Var_Transaction)| ~pred_extract_3(Var_Transaction, term_trustList_0, Var_Claim)| ~pred_trustscheme_2(term_claim_0, term_eIDAS_qualified_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_trustscheme02.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_trustscheme02.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_trustscheme02.tptp", expected);
  }

  @Test
  public void rpx_simple_wrong_verify_signaure01() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, term_transaction_0, Var_Transaction)| ~pred_extract_3(Var_Transaction, term_document_0, Var_Document)| ~pred_extract_3(Var_Transaction, term_issuerKey_0, Var_PkIssuer)| ~pred_verify_signature_2(term_document_0, Var_PkIssuer))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_verify_signaure01.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_verify_signaure01.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_verify_signaure01.tptp", expected);
  }

  @Test
  public void rpx_simple_wrong_verify_signaure02() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_main_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_main_1(Var_Input)| ~pred_extract_3(Var_Input, term_transaction_0, Var_Transaction)| ~pred_extract_3(Var_Transaction, term_document_0, Var_Document)| ~pred_extract_3(Var_Transaction, term_issuerKey_0, Var_PkIssuer)| ~pred_verify_signature_2(Var_Document, term_pkIssuer_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_wrong_verify_signaure02.tpl"
            , "main(Input).", "Input", false, "src/test/targets/rpx_simple_wrong_verify_signaure02.tptp");

    checkRPx("src/test/targets/rpx_simple_wrong_verify_signaure02.tptp", expected);
  }

  @Test
  public void rpx_paper_policy_specification_01() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_accept_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_accept_1(Var_Form)| ~pred_extract_3(Var_Form, term_format_0, term_theAuctionHouse2019Format_0)| ~pred_extract_3(Var_Form, term_bid_0, Var_Bid)| ~op_lseq(Var_Bid, term_100_0))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_format_0, term_theAuctionHouse2019Format_0))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_bid_0, term_99_0))).\n" +
            "cnf(tpl,axiom,(op_lseq(term_99_0, term_100_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/paper_policy_specification_01.tpl"
            , "accept(Input).", "Input", true,
            new AtvApiDummy()
                    .addDocumentEntry("theAuctionHouse2019Format", "input", "format")
                    .addDocumentEntry(99, "input", "bid")
            , "src/test/targets/rpx_paper_policy_specification_01.tptp");

    checkRPx("src/test/targets/rpx_paper_policy_specification_01.tptp",expected);
  }

  @Test
  public void rpx_paper_policy_specification_02() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_accept_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_accept_1(Var_Form)| ~pred_extract_3(Var_Form, term_format_0, term_theAuctionHouse2019format_0)| ~pred_extract_3(Var_Form, term_bid_0, Var_Bid)| ~op_lseq(Var_Bid, term_1500_0)| ~pred_extract_3(Var_Form, term_certificate_0, Var_Certificate)| ~pred_extract_3(Var_Certificate, term_pubKey_0, Var_PK)| ~pred_verify_signature_2(Var_Form, Var_PK)| ~pred_check_eIDAS_qualified_1(Var_Certificate))).\n" +
            "cnf(tpl,axiom,(pred_check_eIDAS_qualified_1(Var_Certificate)| ~pred_extract_3(Var_Certificate, term_format_0, term_eIDAS_qualified_certificate_0)| ~pred_extract_3(Var_Certificate, term_issuer_0, Var_IssuerCertificate)| ~pred_extract_3(Var_IssuerCertificate, term_trustScheme_0, Var_TrustSchemeClaim)| ~pred_trustscheme_2(Var_TrustSchemeClaim, term_eIDAS_qualified_0)| ~pred_trustlist_3(Var_TrustSchemeClaim, Var_IssuerCertificate, Var_TrustListEntry)| ~pred_extract_3(Var_TrustListEntry, term_pubKey_0, Var_PkIss)| ~pred_verify_signature_2(Var_Certificate, Var_PkIss))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_format_0, term_theAuctionHouse2019format_0))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_bid_0, term_8_0))).\n" +
            "cnf(tpl,axiom,(op_lseq(term_8_0, term_1500_0))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_certificate_0, obj_input_certificate))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_certificate, term_pubKey_0, obj_input_certificate_pubKey))).\n" +
            "cnf(tpl,axiom,(pred_verify_signature_2(obj_input, obj_input_certificate_pubKey))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_certificate, term_format_0, term_eIDAS_qualified_certificate_0))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_certificate, term_issuer_0, obj_input_certificate_issuer))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_certificate_issuer, term_trustScheme_0, obj_input_certificate_issuer_trustScheme))).\n" +
            "cnf(tpl,axiom,(pred_trustscheme_2(obj_input_certificate_issuer_trustScheme, term_eIDAS_qualified_0))).\n" +
            "cnf(tpl,axiom,(pred_trustlist_3(obj_input_certificate_issuer_trustScheme, obj_input_certificate_issuer, obj_trustListEntry))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_trustListEntry, term_pubKey_0, obj_trustListEntry_pubKey))).\n" +
            "cnf(tpl,axiom,(pred_verify_signature_2(obj_input_certificate, obj_trustListEntry_pubKey))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/paper_policy_specification_02.tpl"
            , "accept(Input).", "Input", true,
            new AtvApiDummy()
                    .addDocumentEntry("theAuctionHouse2019format", "input", "format")
                    .addDocumentEntry(8, "input", "bid")
                    .addDocumentEntry("eIDAS_qualified_certificate", "input", "certificate", "format")
                    .addDocumentEntry("eIDAS_qualified", "input", "certificate", "issuer", "trustScheme")
            , "src/test/targets/rpx_paper_policy_specification_02.tptp");

    checkRPx("src/test/targets/rpx_paper_policy_specification_02.tptp",expected);
  }

  @Test
  public void rpx_paper_policy_specification_03() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_accept_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_accept_1(Var_Input)| ~pred_extract_3(Var_Input, term_document_0, Var_Document)| ~pred_extract_3(Var_Input, term_mandate_0, Var_Mandate)| ~pred_checkQualifiedDelegation_2(Var_Document, Var_Mandate))).\n" +
            "cnf(tpl,axiom,(pred_checkQualifiedDelegation_2(Var_Document, Var_Mandate)| ~pred_checkMandate_2(Var_Document, Var_Mandate)| ~pred_checkMandatorKey_2(Var_Document, Var_Mandate)| ~pred_checkValidDelegation_2(Var_Document, Var_Mandate)| ~pred_extract_3(Var_Document, term_bid_0, Var_BID)| ~op_lseq(Var_BID, term_1000_0))).\n" +
            "cnf(tpl,axiom,(pred_checkMandate_2(Var_Document, Var_Mandate)| ~pred_extract_3(Var_Mandate, term_format_0, term_delegation_0)| ~pred_extract_3(Var_Mandate, term_proxyKey_0, Var_PkSig)| ~pred_verify_signature_2(Var_Document, Var_PkSig)| ~pred_extract_3(Var_Mandate, term_purpose_0, term_place_bid_0))).\n" +
            "cnf(tpl,axiom,(pred_checkMandatorKey_2(Var_Document, Var_Mandate)| ~pred_extract_3(Var_Mandate, term_issuer_0, Var_MandatorCert)| ~pred_extract_3(Var_MandatorCert, term_trustScheme_0, Var_TrustSchemeClaim)| ~pred_trustscheme_2(Var_TrustSchemeClaim, term_eIDAS_qualified_0)| ~pred_trustlist_3(Var_TrustSchemeClaim, Var_MandatorCert, Var_TrustListEntry)| ~pred_extract_3(Var_TrustListEntry, term_pubKey_0, Var_PkIss)| ~pred_verify_signature_2(Var_MandatorCert, Var_PkIss))).\n" +
            "cnf(tpl,axiom,(pred_checkValidDelegation_2(Var_Document, Var_Mandate)| ~pred_extract_3(Var_Mandate, term_delegationProvider_0, Var_DP)| ~pred_lookup_2(Var_DP, Var_DPEntry)| ~pred_extract_3(Var_DPEntry, term_fingerprint_0, Var_HMandate)| ~pred_verify_hash_2(Var_Mandate, Var_HMandate))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_document_0, obj_input_document))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_mandate_0, obj_input_mandate))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_mandate, term_format_0, term_delegation_0))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_mandate, term_proxyKey_0, obj_input_mandate_proxyKey))).\n" +
            "cnf(tpl,axiom,(pred_verify_signature_2(obj_input_document, obj_input_mandate_proxyKey))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_mandate, term_purpose_0, term_place_bid_0))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_mandate, term_issuer_0, obj_input_mandate_issuer))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_mandate_issuer, term_trustScheme_0, obj_input_mandate_issuer_trustScheme))).\n" +
            "cnf(tpl,axiom,(pred_trustscheme_2(obj_input_mandate_issuer_trustScheme, term_eIDAS_qualified_0))).\n" +
            "cnf(tpl,axiom,(pred_trustlist_3(obj_input_mandate_issuer_trustScheme, obj_input_mandate_issuer, obj_trustListEntry))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_trustListEntry, term_pubKey_0, obj_trustListEntry_pubKey))).\n" +
            "cnf(tpl,axiom,(pred_verify_signature_2(obj_input_mandate_issuer, obj_trustListEntry_pubKey))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_mandate, term_delegationProvider_0, obj_input_mandate_delegationProvider))).\n" +
            "cnf(tpl,axiom,(pred_lookup_2(obj_input_mandate_delegationProvider, obj_))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_, term_fingerprint_0, obj_fingerprint))).\n" +
            "cnf(tpl,axiom,(pred_verify_hash_2(obj_input_mandate, obj_fingerprint))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input_document, term_bid_0, term_8_0))).\n" +
            "cnf(tpl,axiom,(op_lseq(term_8_0, term_1000_0))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/paper_policy_specification_03.tpl"
            , "accept(Input).", "Input", true,
            new AtvApiDummy()
                    .addDocumentEntry(8, "input", "document", "bid")
                    .addDocumentEntry("delegation", "input", "mandate", "format")
                    .addDocumentEntry("place_bid", "input", "mandate", "purpose")
            , "src/test/targets/rpx_paper_policy_specification_03.tptp");

    checkRPx("src/test/targets/rpx_paper_policy_specification_03.tptp",expected);
  }

  @Test
  public void rpx_recursion_problem_test() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_accept_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_accept_1(Var_Input)| ~pred_one_1(Var_Input)| ~pred_two_1(Var_Input))).\n" +
            "cnf(tpl,axiom,(pred_one_1(Var_I)| ~pred_trust_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_0)).\n" +
            "cnf(tpl,axiom,(pred_two_1(Var_I)| ~pred_bla_0)).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/recursion_problem_test.tpl"
            , "accept(Input).", "Input", false, "src/test/targets/rpx_recursion_problem_test.tptp");

    checkRPx("src/test/targets/rpx_recursion_problem_test.tptp", expected);
  }

  @Test
  public void rpx_simple_contract() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_accept_1(obj_input)).\n" +
            "cnf(tpl,axiom,(pred_accept_1(Var_Form)| ~pred_extract_3(Var_Form, term_format_0, term_simpleContract_0)| ~pred_extract_3(Var_Form, term_contract_0, Var_Contract)| ~pred_print_1(Var_Contract))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_format_0, term_simpleContract_0))).\n" +
            "cnf(tpl,axiom,(pred_extract_3(obj_input, term_contract_0, obj_input_contract))).\n" +
            "cnf(tpl,axiom,(pred_print_1(obj_input_contract))).\n";
    runTestRPx("src/test/testdata/generator_tests/advanced_tests/simple_contract.tpl"
            , "accept(Form).", "Form", true,
            new AtvApiDummy()
                    .addDocumentEntry("simpleContract","input","format"), "src/test/targets/rpx_simple_contract.tptp");

    checkRPx("src/test/targets/rpx_simple_contract.tptp", expected);
  }

  @Test
  public void rpx_id() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_id_2(term_c_0, term_c_0)).\n" +
            "cnf(tpl,axiom,(pred_id_2(Var_X, Var_X))).\n";
    runTestRPx("src/test/testdata/generator_tests/id.tpl"
            , "id(c,c).", "Form", true, "src/test/targets/rpx_id.tptp");

    checkRPx("src/test/targets/rpx_id.tptp",expected);
  }

  @Test
  public void rpx_id2() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_id_2(Var_X, Var_Z)).\n" +
            "cnf(tpl,axiom,(pred_id_2(Var_X, Var_X))).\n";
    runTestRPx("src/test/testdata/generator_tests/id.tpl"
            , "id(X,Z).", "Form", true, "src/test/targets/rpx_id2.tptp");

    checkRPx("src/test/targets/rpx_id2.tptp", expected);
  }

  @Test
  public void rpx_id3() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_id_2(term_c_0, term_d_0)).\n" +
            "cnf(tpl,axiom,(pred_id_2(Var_X, Var_X))).\n";
    runTestRPx("src/test/testdata/generator_tests/id.tpl"
            , "id(c,d).", "Form", false, "src/test/targets/rpx_id3.tptp");

    checkRPx("src/test/targets/rpx_id3.tptp", expected);
  }

  @Test
  public void rpx_id4() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_id_2(term_f_3(Var_Y, term_g_1(Var_Z), Var_Y), term_f_3(term_f_1(Var_U), term_g_1(term_c_0), Var_Y))).\n" +
            "cnf(tpl,axiom,(pred_id_2(Var_X, Var_X))).\n";
    runTestRPx("src/test/testdata/generator_tests/id.tpl"
            , "id(f(Y,g(Z),Y), f(f(U),g(c),Y)).", "Form", true, "src/test/targets/rpx_id4.tptp");

    checkRPx("src/test/targets/rpx_id4.tptp", expected);
  }

  @Test
  public void rpx_compositeTerm() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_ftrust_1(term_a_0)).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_f_1(term_a_0)))).\n" +
            "cnf(tpl,axiom,(pred_trust_1(term_g_1(term_b_0)))).\n" +
            "cnf(tpl,axiom,(pred_ftrust_1(Var_X)| ~pred_trust_1(term_f_1(Var_X)))).\n";
    runTestRPx("src/test/testdata/generator_tests/composite_term.tpl"
            , "ftrust(a).", "Form", true, "src/test/targets/rpx_compositeTerm.tptp");

    checkRPx("src/test/targets/rpx_compositeTerm.tptp", expected);
  }

  @Test
  public void rpx_list() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_isMember_2(term_b_0, term_cons_2(term_a_0, term_cons_2(term_b_0, term_cons_2(term_c_0, term_empty_0))))).\n" +
            "cnf(tpl,axiom,(pred_isMember_2(Var_X, term_cons_2(Var_X, Var_L)))).\n" +
            "cnf(tpl,axiom,(pred_isMember_2(Var_X, term_cons_2(Var_Y, Var_L))| ~pred_isMember_2(Var_X, Var_L))).\n" +
            "cnf(tpl,axiom,(pred_doAppend_3(term_empty_0, Var_L, Var_L))).\n" +
            "cnf(tpl,axiom,(pred_doAppend_3(term_cons_2(Var_X, Var_L), Var_M, term_cons_2(Var_X, Var_N))| ~pred_doAppend_3(Var_L, Var_M, Var_N))).\n" +
            "cnf(tpl,axiom,(pred_getde_1(term_cons_2(term_d_0, term_cons_2(term_e_0, term_empty_0))))).\n" +
            "cnf(tpl,axiom,(pred_getabc_1(term_cons_2(term_a_0, term_cons_2(term_b_0, term_cons_2(term_c_0, term_empty_0)))))).\n" +
            "cnf(tpl,axiom,(pred_getdeabc_1(term_cons_2(term_d_0, term_cons_2(term_e_0, Var_X)))| ~pred_getabc_1(Var_X))).\n" +
            "cnf(tpl,axiom,(pred_test_0| ~pred_getde_1(Var_DE)| ~pred_getabc_1(Var_ABC)| ~pred_doAppend_3(Var_DE, Var_ABC, Var_DEABC)| ~pred_getdeabc_1(Var_DEABC))).\n" +
            "cnf(tpl,axiom,(pred_failtest_0| ~pred_getde_1(Var_DE)| ~pred_doAppend_3(Var_DE, Var_ABC, Var_ABC)| ~pred_getabc_1(Var_ABC))).\n";
    runTestRPx("src/test/testdata/generator_tests/list.tpl"
            , "isMember(b,cons(a,cons(b,cons(c,empty)))).", "Form", true, "src/test/targets/rpx_list.tptp");

    checkRPx("src/test/targets/rpx_list.tptp", expected);
  }

  @Test
  public void rpx_list2() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_isMember_2(term_d_0, term_cons_2(term_a_0, term_cons_2(term_b_0, term_cons_2(term_c_0, term_empty_0))))).\n" +
            "cnf(tpl,axiom,(pred_isMember_2(Var_X, term_cons_2(Var_X, Var_L)))).\n" +
            "cnf(tpl,axiom,(pred_isMember_2(Var_X, term_cons_2(Var_Y, Var_L))| ~pred_isMember_2(Var_X, Var_L))).\n" +
            "cnf(tpl,axiom,(pred_doAppend_3(term_empty_0, Var_L, Var_L))).\n" +
            "cnf(tpl,axiom,(pred_doAppend_3(term_cons_2(Var_X, Var_L), Var_M, term_cons_2(Var_X, Var_N))| ~pred_doAppend_3(Var_L, Var_M, Var_N))).\n" +
            "cnf(tpl,axiom,(pred_getde_1(term_cons_2(term_d_0, term_cons_2(term_e_0, term_empty_0))))).\n" +
            "cnf(tpl,axiom,(pred_getabc_1(term_cons_2(term_a_0, term_cons_2(term_b_0, term_cons_2(term_c_0, term_empty_0)))))).\n" +
            "cnf(tpl,axiom,(pred_getdeabc_1(term_cons_2(term_d_0, term_cons_2(term_e_0, Var_X)))| ~pred_getabc_1(Var_X))).\n" +
            "cnf(tpl,axiom,(pred_test_0| ~pred_getde_1(Var_DE)| ~pred_getabc_1(Var_ABC)| ~pred_doAppend_3(Var_DE, Var_ABC, Var_DEABC)| ~pred_getdeabc_1(Var_DEABC))).\n" +
            "cnf(tpl,axiom,(pred_failtest_0| ~pred_getde_1(Var_DE)| ~pred_doAppend_3(Var_DE, Var_ABC, Var_ABC)| ~pred_getabc_1(Var_ABC))).\n";
    runTestRPx("src/test/testdata/generator_tests/list.tpl"
            , "isMember(d,cons(a,cons(b,cons(c,empty)))).", "Form", false, "src/test/targets/rpx_list2.tptp");

    checkRPx("src/test/targets/rpx_list2.tptp", expected);
  }

  @Test
  public void rpx_list3() {
    String expected = "% The TrustPolicyInterpreter found a solution. Therefore, the query should follow from the query and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_test_0).\n" +
            "cnf(tpl,axiom,(pred_isMember_2(Var_X, term_cons_2(Var_X, Var_L)))).\n" +
            "cnf(tpl,axiom,(pred_isMember_2(Var_X, term_cons_2(Var_Y, Var_L))| ~pred_isMember_2(Var_X, Var_L))).\n" +
            "cnf(tpl,axiom,(pred_doAppend_3(term_empty_0, Var_L, Var_L))).\n" +
            "cnf(tpl,axiom,(pred_doAppend_3(term_cons_2(Var_X, Var_L), Var_M, term_cons_2(Var_X, Var_N))| ~pred_doAppend_3(Var_L, Var_M, Var_N))).\n" +
            "cnf(tpl,axiom,(pred_getde_1(term_cons_2(term_d_0, term_cons_2(term_e_0, term_empty_0))))).\n" +
            "cnf(tpl,axiom,(pred_getabc_1(term_cons_2(term_a_0, term_cons_2(term_b_0, term_cons_2(term_c_0, term_empty_0)))))).\n" +
            "cnf(tpl,axiom,(pred_getdeabc_1(term_cons_2(term_d_0, term_cons_2(term_e_0, Var_X)))| ~pred_getabc_1(Var_X))).\n" +
            "cnf(tpl,axiom,(pred_test_0| ~pred_getde_1(Var_DE)| ~pred_getabc_1(Var_ABC)| ~pred_doAppend_3(Var_DE, Var_ABC, Var_DEABC)| ~pred_getdeabc_1(Var_DEABC))).\n" +
            "cnf(tpl,axiom,(pred_failtest_0| ~pred_getde_1(Var_DE)| ~pred_doAppend_3(Var_DE, Var_ABC, Var_ABC)| ~pred_getabc_1(Var_ABC))).\n";
    runTestRPx("src/test/testdata/generator_tests/list.tpl"
            , "test().", "Form", true, "src/test/targets/rpx_list3.tptp");

    checkRPx("src/test/targets/rpx_list3.tptp",expected);
  }

  @Test
  public void rpx_list4() {
    String expected = "% The TrustPolicyInterpreter did not find a solution. Therefore, the query may or may not follow from the policy and environment.\n" +
            "cnf(tpl,negated_conjecture, ~pred_failtest_0).\n" +
            "cnf(tpl,axiom,(pred_isMember_2(Var_X, term_cons_2(Var_X, Var_L)))).\n" +
            "cnf(tpl,axiom,(pred_isMember_2(Var_X, term_cons_2(Var_Y, Var_L))| ~pred_isMember_2(Var_X, Var_L))).\n" +
            "cnf(tpl,axiom,(pred_doAppend_3(term_empty_0, Var_L, Var_L))).\n" +
            "cnf(tpl,axiom,(pred_doAppend_3(term_cons_2(Var_X, Var_L), Var_M, term_cons_2(Var_X, Var_N))| ~pred_doAppend_3(Var_L, Var_M, Var_N))).\n" +
            "cnf(tpl,axiom,(pred_getde_1(term_cons_2(term_d_0, term_cons_2(term_e_0, term_empty_0))))).\n" +
            "cnf(tpl,axiom,(pred_getabc_1(term_cons_2(term_a_0, term_cons_2(term_b_0, term_cons_2(term_c_0, term_empty_0)))))).\n" +
            "cnf(tpl,axiom,(pred_getdeabc_1(term_cons_2(term_d_0, term_cons_2(term_e_0, Var_X)))| ~pred_getabc_1(Var_X))).\n" +
            "cnf(tpl,axiom,(pred_test_0| ~pred_getde_1(Var_DE)| ~pred_getabc_1(Var_ABC)| ~pred_doAppend_3(Var_DE, Var_ABC, Var_DEABC)| ~pred_getdeabc_1(Var_DEABC))).\n" +
            "cnf(tpl,axiom,(pred_failtest_0| ~pred_getde_1(Var_DE)| ~pred_doAppend_3(Var_DE, Var_ABC, Var_ABC)| ~pred_getabc_1(Var_ABC))).\n";
    runTestRPx("src/test/testdata/generator_tests/list.tpl"
            , "failtest().", "Form", false, "src/test/targets/rpx_list4.tptp");

    checkRPx("src/test/targets/rpx_list4.tptp",expected);
  }
}

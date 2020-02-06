import eu.lightest.horn.Interpreter;
import eu.lightest.horn.exceptions.HornFailedException;
import eu.lightest.horn.util.AtvApiDummy;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


public class TestGenerator {
  private final ByteArrayOutputStream mOutContent = new ByteArrayOutputStream();
  private final PrintStream oldOut = System.out;

  /**
   * Further TODO:
   * - Deeper Relops
   * - Deeper Arithm
   * - Decouple data form logic
   */

  private void runTest(String pathToTpl, String q, String inputVariable, boolean shouldBe, AtvApiDummy api) {
    System.out.println("path= " + System.getProperty("user.dir"));
    Interpreter m = new Interpreter(api);
    try {
      Assert.assertEquals(shouldBe, m.run(pathToTpl, q, inputVariable));
    } catch (HornFailedException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }
  private void checkException(String pathToTpl, String q, String inputVariable, HornFailedException shouldBe, AtvApiDummy api) {
    System.out.println("path= " + System.getProperty("user.dir"));
    Interpreter m = new Interpreter(api);
    try {
      m.run(pathToTpl, q, inputVariable);
    } catch (HornFailedException e) {
      Assert.assertEquals(shouldBe.getMessage(), e.getMessage());
//      Assert.assertEquals(shouldBe, e);
    }
  }

  private void runTest(String pathToTpl, String q, String inputVariable, boolean shouldBe) {
    runTest(pathToTpl, q, inputVariable, shouldBe, new AtvApiDummy());
  }

  private void runOutputTest(String pathToTpl, String q, String inputVariable, boolean shouldBe, String targetString, AtvApiDummy api) {
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
        Assert.fail();
      }
    } finally {
      //end
      System.setOut(oldOut);
      System.out.println(mOutContent);
    }
  }

  private void runOutputTest(String pathToTpl, String q, String inputVariable, boolean shouldBe, String targetString) {
    runOutputTest(pathToTpl, q, inputVariable, shouldBe, targetString, new AtvApiDummy());
  }

  @Test
  public void testing_correct_print_of_syntax_error() {
    checkException("src/test/testdata/generator_tests/testing_correct_print_of_syntax_error.tpl", "trust(a).",
        "Input",
        new HornFailedException( "In program:\n" +
            "Number of syntax errors: 1\n" +
            "  #1: line7:19 mismatched input ',' expecting {RELOP, ARITH}\n"), new AtvApiDummy());
  }

  @Test
  public void testing_correct_print_of_lexer_error() {
    checkException("src/test/testdata/generator_tests/testing_correct_print_of_lexer_error.tpl", "trust(a).", "Input",
        new HornFailedException("In program:\n" +
            "Number of lexical errors: 1\n" +
            "  #1: line7:19 token recognition error at: '#'\n"), new AtvApiDummy());
  }

  @Test
  public void empty_query_test() {
    checkException("src/test/testdata/generator_tests/empty_test.tpl"
        , "", "Input", new HornFailedException("No Valid query is given to the Interpreter!"), new AtvApiDummy());
  }

  @Test
  public void arithm_test() {
    checkException("src/test/testdata/generator_tests/arithm_test.tpl"
        , "trust(e, 4).", "input", new HornFailedException("In program:\n" +
            "Number of type errors: 1\n" +
            "  #1: line7:45 Only simple Arithmetic Operations are allowed\n"), new AtvApiDummy());
  }

  //------------ Interpreter Tests ------------------------

  @Test
  public void wrong_query() {
    checkException("src/test/testdata/generator_tests/simple_trust.tpl"
        , "trust(ab.", "Input", new HornFailedException("In query:\n" +
            "Number of syntax errors: 1\n" +
            "  #1: line1:8 no viable alternative at input 'trust(ab.'\n"), new AtvApiDummy());
  }

  @Test
  public void simple_trust_a() {
    runTest("src/test/testdata/generator_tests/simple_trust.tpl"
            , "trust(a).", "Input", true);
  }

  @Test
  public void simple_trust_a_query_not() {
    runTest("src/test/testdata/generator_tests/simple_trust.tpl"
        , "trust(not(a)).", "Input", false);
  }

  @Test
  public void simple_trust_XA() {
    runTest("src/test/testdata/generator_tests/simple_trust_x.tpl"
        , "trust(a).", "Input", true);
  }

  @Test
  public void simple_trust_X_2() {
    runTest("src/test/testdata/generator_tests/simple_trust_x.tpl"
        , "trust(X).", "Input", true);
  }

  @Ignore
  @Disabled
  @Test
  public void multi_solutions() { //TODO implement multi solution
    runOutputTest("src/test/testdata/generator_tests/simple_multi_sol.tpl"
        , "trust(X).", "Input", true, "Found multiple Outputs: trust(a), trust(b), trust(c)");
  }

  @Test
  public void TrustDelegationProgramSimpleFindTrust() {
    runTest("src/test/testdata/generator_tests/trust_delegation.tpl"
        , "trust(a).", "Input", true);
  }

  @Test
  public void TrustDelegationProgram() {
    runTest("src/test/testdata/generator_tests/trust_delegation.tpl"
        , "trust(c).", "Input", true);
  }

  @Test
  public void only_entities() {
    runTest("src/test/testdata/generator_tests/only_entities.tpl"
        , "trust(a).", "Input", true);
  }

  @Test
  public void trust_delegation_wrong_then_right_facts() {
    runTest("src/test/testdata/generator_tests/two_possible_facts_correct.tpl"
        , "trust(c).", "Input", true);
  }

  @Test
  public void trust_delegation_wrong_then_wrong_facts() {
    runTest("src/test/testdata/generator_tests/two_possible_facts_wrong.tpl"
        , "trust(c).", "Input", false);
  }

  @Test
  public void two_possible_rules() {
    runTest("src/test/testdata/generator_tests/two_possible_rules.tpl"
        , "trust(c).", "Input", true);
  }

  @Test
  public void TrustDelegationProgram_half() {
    runTest("src/test/testdata/generator_tests/trust_delegation.tpl"
        , "trust(b).", "Input", true);
  }

  @Test
  public void TrustDelegationProgram_special01() {
    runTest("src/test/testdata/generator_tests/trust_delegation_special01.tpl"
        , "trust(c).", "Input", true);
  }

  @Test
  public void TrustDelegationProgram_special02() { // TODO throw warning exception
    runTest("src/test/testdata/generator_tests/trust_delegation_special02.tpl"
        , "trust(c).", "Input", true);
  }

  @Test
  public void TrustDelegationProgram_doubleTrust() { // TODO two outputs
    runTest("src/test/testdata/generator_tests/trust_delegation_doubleTrust.tpl"
        , "trust(e).", "Input", true);
  }

  @Test
  public void TrustDelegationProgramKasWurst() {
    runTest("src/test/testdata/generator_tests/trust_delegation_kasWurst.tpl"
        , "trust(c).", "Input", true);
  }

  @Ignore
  @Disabled
  @Test
  public void InterestingTpl() {
    runTest("src/test/testdata/generator_tests/interesting.tpl"
        , "trust(v).", "Input", true);
  }

  @Test
  public void sebs_intro_prolog_test01() {
    runTest("src/test/testdata/generator_tests/sebs_intro_prolog_test.tpl"
        , "trust(e, 2).", "Input", true);
  }

  @Test
  public void sebs_intro_prolog_test02() {
    runTest("src/test/testdata/generator_tests/sebs_intro_prolog_test.tpl"
        , "trust(e, 1).", "Input", false);
  }

  @Test
  public void sebs_detail_definition_example01() {
    runTest("src/test/testdata/generator_tests/sebs_detail_definition_example.tpl"
        , "trust(a).", "Input", true);
  }

  @Test
  public void sebs_detail_definition_example02() {
    runTest("src/test/testdata/generator_tests/sebs_detail_definition_example.tpl"
        , "trust(b).", "Input", false);
  }

  @Test
  public void sebs_detail_definition_example03() {
    runTest("src/test/testdata/generator_tests/sebs_detail_definition_example.tpl"
        , "trust(c).", "Input", true);
  }


  @Test
  public void circular_inf_loop() {
    checkException("src/test/testdata/generator_tests/simple_circular.tpl"
        , "trust(d).", "Input", new HornFailedException("Infinity loop error: Depth bound exceeded"), new AtvApiDummy());
  }

  @Ignore
  @Disabled//probably wrong
  @Test
  public void relop_with_term() {
    runOutputTest("src/test/testdata/generator_tests/relop_with_term.tpl"
        , "trust(d).", "Input", false, " are not meant for Relop!");
  }

  @Test
  public void avr_without_value() { // TODO throw Warning
    runTest("src/test/testdata/generator_tests/avr_without_value.tpl"
        , "trust(d).", "Input", false /*, "Warning: Variable without any chance of getting a Value!"*/);
  }

  /**
   * keyword plugin system tests ----------------------------------------------------------------
   */

  @Test
  public void simple_print() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_print.tpl"
        , "main(Input).", "Input", true);
  }

  //TODO make tests with wrong var types and so on....
  @Test
  public void simple_extract() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_extract_test.tpl"
        , "main(Input).", "Input", true);
  }

  @Test
  public void simple_extract_with_const() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_extract_test_const.tpl"
        , "main(Input).", "Input", true,
        new AtvApiDummy()
            .addDocumentEntry("theAuctionHouse2018format", "input", "format"));
  }

  @Test
  public void simple_verify_signaure() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_verify_signaure.tpl"
        , "main(Input).", "Input", true);
  }

  @Test
  public void simple_trustscheme() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_trustscheme.tpl"
        , "main(Input).", "Input", true);
  }

  @Test
  public void simple_lookup_test() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_lookup_test.tpl"
        , "main(Input).", "Input", true);
  }

  @Test
  public void simple_wrong_extract_test01() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_wrong_extract_test01.tpl"
        , "main(Input).", "Input", false);
  }


  @Test
  public void simple_wrong_extract_test03() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_wrong_extract_test03.tpl"
        , "main(Input).", "Input", false);
  }

  @Test
  public void simple_wrong_lookup_test01() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_wrong_lookup_test01.tpl"
        , "main(Input).", "Input", false);
  }

  @Test
  public void simple_wrong_lookup_test02() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_wrong_lookup_test02.tpl"
        , "main(Input).", "Input", false);
  }

  @Test
  public void simple_print02() {
    runOutputTest("src/test/testdata/generator_tests/advanced_tests/simple_print02.tpl"
        , "main(Input).", "Input", true, "Horn says: input");
  }

  // TODO: The output string cannot contain the variable "Input" as this variable is instantiated to a TplTermObject
  //       before printing occurs. I have given my own version of this test below, which I believe to be correct.
  /* @Test
  public void simple_print03() {
    runOutputTest("src/test/testdata/generator_tests/advanced_tests/simple_print03.tpl"
        , "main(Input).", true, "<interpreter>: Input");
  } */
  @Test
  public void simple_print03() {
    runOutputTest("src/test/testdata/generator_tests/advanced_tests/simple_print03.tpl"
        , "main(Input).", "Input", true, "Horn says: [input]");
  }

  @Test
  public void simple_wrong_trustscheme01() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_wrong_trustscheme01.tpl"
        , "main(Input).", "Input", false);
  }

  @Test
  public void simple_wrong_trustscheme02() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_wrong_trustscheme02.tpl"
        , "main(Input).", "Input", false);
  }

  @Test
  public void simple_wrong_verify_signaure01() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_wrong_verify_signaure01.tpl"
        , "main(Input).", "Input", false);
  }

  @Test
  public void simple_wrong_verify_signaure02() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_wrong_verify_signaure02.tpl"
        , "main(Input).", "Input", false);
  }

  @Test
  public void paper_policy_specification_01() {
    runTest("src/test/testdata/generator_tests/advanced_tests/paper_policy_specification_01.tpl"
        , "accept(Input).", "Input", true,
        new AtvApiDummy()
            .addDocumentEntry("theAuctionHouse2019Format", "input", "format")
            .addDocumentEntry(99, "input", "bid")
    );
  }

  @Test
  public void paper_policy_specification_02() {
    runTest("src/test/testdata/generator_tests/advanced_tests/paper_policy_specification_02.tpl"
        , "accept(Input).", "Input", true,
        new AtvApiDummy()
            .addDocumentEntry("theAuctionHouse2019format", "input", "format")
            .addDocumentEntry(8, "input", "bid")
            .addDocumentEntry("eIDAS_qualified_certificate", "input", "certificate", "format")
            .addDocumentEntry("eIDAS_qualified", "input", "certificate", "issuer", "trustScheme")
    );
  }

  @Test
  public void paper_policy_specification_03() {
    runTest("src/test/testdata/generator_tests/advanced_tests/paper_policy_specification_03.tpl"
        , "accept(Input).", "Input", true,
        new AtvApiDummy()
            .addDocumentEntry(8, "input", "document", "bid")
            .addDocumentEntry("delegation", "input", "mandate", "format")
            .addDocumentEntry("place_bid", "input", "mandate", "purpose")
    );
  }

  @Test
  public void recursion_problem_test() { //FIXME
    runTest("src/test/testdata/generator_tests/advanced_tests/recursion_problem_test.tpl"
        , "accept(Input).", "Input", false);
  }

  @Test
  public void simple_contract() {
    runTest("src/test/testdata/generator_tests/advanced_tests/simple_contract.tpl"
        , "accept(Form).", "Form", true,
        new AtvApiDummy()
            .addDocumentEntry("simpleContract", "input", "format"));
  }

  @Test
  public void id() {
    runTest("src/test/testdata/generator_tests/id.tpl"
        , "id(c,c).", "Form", true);
  }

  @Test
  public void id2() {
    runTest("src/test/testdata/generator_tests/id.tpl"
        , "id(X,Z).", "Form", true);
  }

  @Test
  public void id3() {
    runTest("src/test/testdata/generator_tests/id.tpl"
        , "id(c,d).", "Form", false);
  }

  @Test
  public void id4() {
    runTest("src/test/testdata/generator_tests/id.tpl"
        , "id(f(Y,g(Z),Y), f(f(U),g(c),Y)).", "Form", true);
  }

  @Test
  public void compositeTerm() {
    runTest("src/test/testdata/generator_tests/composite_term.tpl"
        , "ftrust(a).", "Form", true);
  }

  @Test
  public void list() {
    runTest("src/test/testdata/generator_tests/list.tpl"
        , "isMember(b,cons(a,cons(b,cons(c,empty)))).", "Form", true);
  }

  @Test
  public void list2() {
    runTest("src/test/testdata/generator_tests/list.tpl"
        , "isMember(d,cons(a,cons(b,cons(c,empty)))).", "Form", false);
  }

  @Test
  public void list3() {
    runTest("src/test/testdata/generator_tests/list.tpl"
        , "test().", "Form", true);
  }

  @Test
  public void list4() {
    runTest("src/test/testdata/generator_tests/list.tpl"
        , "failtest().", "Form", false);
  }

  @Test
  public void missleading_predicate() {
    runTest("src/test/testdata/generator_tests/missleading_predicate.tpl"
        , "accept(Form).", "Form", false, // or should it be true?
        new AtvApiDummy());
  }

  @Test
  public void missleading_predicate2() {
    runTest("src/test/testdata/generator_tests/missleading_predicate2.tpl"
        , "accept(Form).", "Form", false, // or should it be true?
        new AtvApiDummy());
  }

  @Test
  public void firstExample() {
    runTest("src/test/testdata/generator_tests/TranslationTests/firstExample.tpl"
        , "accept(Transaction).", "Transaction", true,
        new AtvApiDummy()
            .addDocumentEntry("auctionHouse2019", "input", "format")
            .addDocumentEntry("x509cert", "input", "certificate", "format")
            .addDocumentEntry("eIDqualified", "trustListEntry", "serviceType")
            .addDocumentEntry("generic_trustlist_format", "trustListEntry", "format")
            .addDocumentEntry("true", "trustListEntry", "signedInPerson"));
  }

//  @Ignore
//  @Disabled
  @Test
  public void completeNonTranslation() { //TODO DTU should this work?
    runTest("src/test/testdata/generator_tests/TranslationTests/completeNonTranslation.tpl"
        , "accept(Transaction).", "Transaction", true,
        new AtvApiDummy()
            .addDocumentEntry("auctionHouse2019", "input", "format")
            .addDocumentEntry("x509cert", "input", "certificate", "format")
            .addDocumentEntry("eIDqualified", "trustListEntry", "serviceType")
            .addDocumentEntry("generic_trustlist_format", "trustListEntry", "format")
            .addDocumentEntry("true", "trustListEntry", "signedInPerson"));
  }

  @Test
  public void set_format_test() { //TODO DTU should this work?
    runOutputTest("src/test/testdata/generator_tests/advanced_tests/set_format_test.tpl"
            , "accept(Form).", "Form", true, "format is set to:" +
                    " <theAuctionHouse2019Format> and input to: <[input]>",
            new AtvApiDummy()
                    .addDocumentEntry("theAuctionHouse2019Format", "input", "format"));
  }

  @Test
  public void backtracking_test1() {
    runTest("src/test/testdata/generator_tests/multiple_formats.tpl"
            , "accept(Form).", "Form", false,
            new AtvApiDummy()
                    .addDocumentEntry("auctionHouseDTU", "input", "format")
                    .addDocumentEntry("6", "input", "bid")
    );
  }

  @Test
  public void backtracking_test2() {
    runTest("src/test/testdata/generator_tests/multiple_formats.tpl"
            , "accept(Form).", "Form", false,
            new AtvApiDummy()
                    .addDocumentEntry("auctionHouseDTU", "input", "format")
                    .addDocumentEntry(6, "input", "bid")
    );
  }

  @Test
  public void backtracking_test3() {
    runTest("src/test/testdata/generator_tests/multiple_formats.tpl"
            , "accept(Form).", "Form", true,
            new AtvApiDummy()
                    .addDocumentEntry("auctionHouseTUG", "input", "format")
                    .addDocumentEntry(4, "input", "bid")
    );
  }

  @Test
  public void backtracking_test4() {
    runTest("src/test/testdata/generator_tests/policy_pso_simple.tpl"
            , "accept(Form).", "Form", true,
            new AtvApiDummy()
                    .addDocumentEntry("pumpkinSeedOil", "input", "format")
                    .addDocumentEntry(54678, "input", "item_id")
                    .addDocumentEntry(4, "input", "ammount")
    );
  }

  @Test
  public void correos_pilot() {
    runTest("src/test/testdata/generator_tests/Pilots/correos_pilot.tpl"
        , "check_valid_document(Document).", "Document", true,
        new AtvApiDummy()
            .addDocumentEntry("correos_pdf_format", "input", "format")
            .addDocumentEntry("certificate", "input", "seal", "format")
    );
  }

  @Test
  public void stefans_discovery() {
    runTest("src/test/testdata/generator_tests/stefans_discovery.tpl"
            , "test(Form).", "Form", true,
            new AtvApiDummy()
                    .addDocumentEntry(3, "input", "sillinessLevel")
    );
  }

  @Test
  public void stefans_discovery_termobject_relop() {
    runTest("src/test/testdata/generator_tests/stefans_discovery.tpl"
            , "test(Form).", "Form", true,
            new AtvApiDummy()
                    .addDocumentEntry("3", "input", "sillinessLevel")
    );
  }

  @Test
  public void stefans_discovery_termobject_integer_unification() {
    runTest("src/test/testdata/generator_tests/stefans_discovery2.tpl"
            , "test(Form).", "Form", true,
            new AtvApiDummy()
                    .addDocumentEntry("3", "input", "sillinessLevel")
    );
  }

  @Test
  public void negative_numbers_test() {
    runTest("src/test/testdata/generator_tests/negative_numbers_test.tpl"
            , "trust(f,1).", "Form", true
    );
  }

  @Test
  public void constant_literals() {
    runTest("src/test/testdata/generator_tests/constant_literals.tpl"
            , "accept(Input).", "Input", true,
            new AtvApiDummy()
                    .addDocumentEntry("TheAuctionHouse2019Format", "input", "format")
                    .addDocumentEntry(99, "input", "bid")
                    .addDocumentEntry("Hello", "input", "constLit")
                    .addDocumentEntry("Hello", "input", "ConstLit")
    );
  }
  @Test
  @Ignore
  @Disabled
  public void wrong_query_exception() {
    checkException("src/test/testdata/generator_tests/exceptionTests/wrongQuery.tpl"
        , "test(Form).", "Form", new HornFailedException("Query predicate does not exist in policy!"),
        new AtvApiDummy());
  }
}

package eu.lightest.horn;

import eu.lightest.horn.AST.TplPolicy;
import eu.lightest.horn.codegen.CodeGenerator;
import eu.lightest.horn.codegen.CodeGeneratorInterface;
import eu.lightest.horn.error.ErrorHandler;
import eu.lightest.horn.exceptions.*;
import eu.lightest.horn.lex.LexerInterface;
import eu.lightest.horn.lex.LexicalAnalyzer;
import eu.lightest.horn.util.AtvApiDummy;
import eu.lightest.horn.specialKeywords.IAtvApiListener;
import eu.lightest.horn.syntax.SyntaxAnalyzer;
import eu.lightest.horn.syntax.SyntaxInterface;
import eu.lightest.horn.type.TypeChecker;
import eu.lightest.horn.type.TypeCheckerInterface;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;

public class Interpreter {
  final static int MAJOR_VERSION = 0;
  final static int MINOR_VERSION = 1;
  final static int BUILD_VERSION = 0;

  public static IAtvApiListener mAtvApi = new AtvApiDummy();
  public static boolean recordRPxTranscript = false;
  public static String recordRPxTranscriptLocation;

  public Interpreter(IAtvApiListener api) {
    mAtvApi = api;
  }

  private static Logger logger = Logger.getLogger(Interpreter.class);

  public boolean run(String tplFilePath, String query, String inputVariable) throws HornFailedException {
    // 1. Parse the Trust Policy and create the AST
    FileInputStream program_file = null;
    String program_txt = null;
    try {
      program_file = new FileInputStream(tplFilePath);
      program_txt = IOUtils.toString(program_file);
      logger.debug(program_txt);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return runInternal(query, inputVariable, program_txt);
  }

  public boolean runInternal(String query, String inputVariable, String program_txt) throws HornFailedException {
    debug("LIGHTest TPL Parser V"
        + String.valueOf(MAJOR_VERSION)
        + "."
        + String.valueOf(MINOR_VERSION)
        + "."
        + String.valueOf(BUILD_VERSION));
    debug("(c) 2018 LIGHTest Consortium");

    LexerInterface lexer = new LexicalAnalyzer();
    lexer.lexer(program_txt);

    if (ErrorHandler.getInstance().getLexerErrorCount() > 0) {
      ErrorHandler.getInstance().printLexerErrors(false);
      throw new LexerException(ErrorHandler.getInstance().getLexerErrors(false));
    }

    SyntaxInterface syntax = new SyntaxAnalyzer();
    syntax.syntax(lexer.getTokenStream());

    if (ErrorHandler.getInstance().getSyntaxErrorCount() > 0) {
      ErrorHandler.getInstance().printSyntaxErrors(false);
      throw new SyntaxException(ErrorHandler.getInstance().getSyntaxErrors(false));
    }

    TypeCheckerInterface typeChecker = new TypeChecker();
    TplPolicy program = typeChecker.typeCheck(syntax.getParseTree());

    if (ErrorHandler.getInstance().getTypeErrorCount() > 0) {
      ErrorHandler.getInstance().printTypeErrors(false);
      throw new TypeCheckingException(ErrorHandler.getInstance().getTypeErrors(false));
    }

    // 2. Parse the actual question to the system
    LexerInterface queryLexer = new LexicalAnalyzer();
    queryLexer.lexer(query);

    if (ErrorHandler.getInstance().getLexerErrorCount() > 0) {
      ErrorHandler.getInstance().printLexerErrors(true);
      throw new LexerException(ErrorHandler.getInstance().getLexerErrors(true));
    }

    SyntaxInterface querySyntax = new SyntaxAnalyzer();
    querySyntax.syntax(queryLexer.getTokenStream());

    if (ErrorHandler.getInstance().getSyntaxErrorCount() > 0) {
      ErrorHandler.getInstance().printSyntaxErrors(true);
      throw new SyntaxException(ErrorHandler.getInstance().getSyntaxErrors(true));
    }

    TypeCheckerInterface queryTypeChecker = new TypeChecker();
    TplPolicy question = queryTypeChecker.typeCheck(querySyntax.getParseTree());

    if (ErrorHandler.getInstance().getTypeErrorCount() > 0) {
      ErrorHandler.getInstance().printTypeErrors(true);
      throw new TypeCheckingException(ErrorHandler.getInstance().getTypeErrors(true));
    }

    // 3. execution
    CodeGeneratorInterface codeGenerator = new CodeGenerator();

    return codeGenerator.generate(question, inputVariable, program);
  }

  //for external revision process
  public boolean runCodeRev(String program_txt) throws HornFailedException {

    LexerInterface lexer = new LexicalAnalyzer();
    lexer.lexer(program_txt);

    if (ErrorHandler.getInstance().getLexerErrorCount() > 0) {
      ErrorHandler.getInstance().printLexerErrors(false);
      throw new LexerException(ErrorHandler.getInstance().getLexerErrors(false));
    }

    SyntaxInterface syntax = new SyntaxAnalyzer();
    syntax.syntax(lexer.getTokenStream());

    if (ErrorHandler.getInstance().getSyntaxErrorCount() > 0) {
      ErrorHandler.getInstance().printSyntaxErrors(false);
      throw new SyntaxException(ErrorHandler.getInstance().getSyntaxErrors(false));
    }

    TypeCheckerInterface typeChecker = new TypeChecker();
    TplPolicy program = typeChecker.typeCheck(syntax.getParseTree());

    if (ErrorHandler.getInstance().getTypeErrorCount() > 0) {
      ErrorHandler.getInstance().printTypeErrors(true);
      throw new TypeCheckingException(ErrorHandler.getInstance().getTypeErrors(true));
    }

    return true;
  }

  public ParseTree parseQuestion(String question) {
    CodePointCharStream input = CharStreams.fromString(question);
    HornLexer l = new HornLexer(input);
    TokenStream t = new CommonTokenStream(l);
    HornParser p = new HornParser(t);

    return p.program();
  }

  void debug(Object obj) {
    logger.debug(obj.toString());
  }

}

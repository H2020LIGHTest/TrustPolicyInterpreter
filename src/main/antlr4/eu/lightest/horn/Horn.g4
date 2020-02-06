/**
 * Grammar file for basic TPL parsing
 *
 * TODO: Parse an ignore list
 */


grammar Horn;

// Keywords

// Operators

RELOP : '<'
      | '>'
      | '=='
      | '<='
      | '>='
      ;

ARITH : '-'
      | '+'
      | '*'
      ;


// Numerical literals
fragment DIGIT0 : [0-9];
fragment DIGIT  : [1-9];
//INT : ('-'?) (DIGIT(DIGIT0)*) | '0';
INT : (DIGIT(DIGIT0)*) | '0';

// Identifiers
fragment LCCHAR : [a-z];
fragment UCCHAR : [A-Z];
fragment ALPHA : LCCHAR | UCCHAR;
VAR : UCCHAR (ALPHA | DIGIT0)*;

// Could we rename TERM -> IDEN (for identifier)?
IDENTIFIER :
      LCCHAR (ALPHA|DIGIT0|'_')*
    | '\'' ALPHA (ALPHA|DIGIT0|'_')* '\'';
//TERM : LCCHAR ALPHA*;

WS : [ \n\t\r] -> channel(HIDDEN);

// Comment
COMMENT : '%' ~['\n'|'\r']* -> channel(HIDDEN);
BLOCK_COMMENT : '/*' .*? '*/' -> channel(HIDDEN);

/*
 * A program can consist of directives and clauses.
 * Examples (directives)
 * A:-is(B).
 * A:-is(was(C)).
 * A:-is(B,C,D).
 * is(A):-was(B).
 * can(run(A)):-when(B),when(C).
 *
 * Examples(clauses)
 * X.
 * func(X).
 * func(param(X), param(Y)).
 */
/*
 * Terms are the heart of the language.
 * they can hold a variable, another term
 * and can use relops
 *
 * Examples:
 * X
 * func(X)
 * this_is_a_function(with(some(parameters(X,Y,Z),A),B))
 */


//program: clause * EOF;
program:
    clause * EOF;

clause:
    fact body;

body :
    (':-' fact (',' fact)* )? '.';

fact:
      IDENTIFIER '(' terms ')'
    | term RELOP term
    ;

terms:
       term (',' term)*
     |
     ;

term: VAR
    | INT
    | IDENTIFIER ('(' terms ')')?
    | term ARITH term
    ;

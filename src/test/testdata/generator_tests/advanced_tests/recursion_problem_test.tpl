accept(Input) :-
  one(Input),
  two(Input).

one(I) :- trust().

trust().

two(I) :- bla().
nat(N) :- nat(set(N)).
nat(s(N)) :- nat(N).
nat( ).
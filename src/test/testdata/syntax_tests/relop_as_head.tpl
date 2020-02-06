X > 1 :- trust(X).
trust(b).
trust(c).
delegate(c,d).
delegate(d,e).
trust(X, N):- trust(X).

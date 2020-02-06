trust(a).
trust(b).
trust(c).

delegate(c,d).
delegate(d,e).
delegate(e,f).

trust(X, N):- trust(X).
trust(X, N):- N >= 0-2, delegate(Y,X), trust(Y, N - 1).

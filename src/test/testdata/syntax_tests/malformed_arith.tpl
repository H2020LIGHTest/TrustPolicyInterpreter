trust(a).
trust(b).
trust(c).
delegate(c,d).
delegate(d,e).
trust(X, N):- trust(X).
trust(X, N):- N - 0, delegate(Y,X), trust(Y, N - 1).
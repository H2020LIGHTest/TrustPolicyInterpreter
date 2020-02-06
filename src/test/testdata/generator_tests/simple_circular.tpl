delegate(a, d).
delegate(d, c).
delegate(c, d).
trust(X):- delegate(X, Y), trust(Y).

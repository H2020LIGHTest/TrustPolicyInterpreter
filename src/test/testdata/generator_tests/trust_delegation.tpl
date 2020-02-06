trust(a).
delegate(b,a).
delegate(c,b).
trust(X):-delegate(X,Y),trust(Y).

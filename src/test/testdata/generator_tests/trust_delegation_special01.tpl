trust(a).
delegate(b,a).
delegate(c,b).
delegate(c,u).
trust(X):-delegate(X,Y),trust(Y).

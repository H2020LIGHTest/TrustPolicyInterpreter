trust(a).
trust(c).
delegate(b,a).
delegate(c,b).
delegate(d,c).
delegate(e,d).
trust(X):-delegate(X,Y),trust(Y).


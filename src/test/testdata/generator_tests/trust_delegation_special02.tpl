trust(a).
delegate(b,a).
delegate(c,b).
trust(X):-delegate(X,Y),trust(Y).
trust(X):-trust(Y), delegate(X,Y).
trust(X):-trust(Y), delegate(Y,Y).

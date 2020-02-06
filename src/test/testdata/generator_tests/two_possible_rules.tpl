trust(f).
delegate(b,a).
delegate(c,b).
bla(c,e).
bla(e,f).
trust(X):-delegate(X,Y),trust(Y).
trust(X):-bla(X,Y),trust(Y).

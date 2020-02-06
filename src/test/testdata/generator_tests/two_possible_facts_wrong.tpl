trust(z).
delegate(b,a).
delegate(c,b).
delegate(c,e).
delegate(e,f).
trust(X):-delegate(X,Y),trust(Y).

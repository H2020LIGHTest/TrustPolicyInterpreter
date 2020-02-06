trust(a).
kaswurst(b,a).
kaswurst(c,b).
trust(X):-kaswurst(X,Y),trust(Y).

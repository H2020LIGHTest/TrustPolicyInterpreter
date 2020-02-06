trust(a).
trust(b).
trust(c).
delegate(c,d).
delegate(d,e).
trust(X):- N > 0, delegate(Y,X), trust(Y, N - 1).
% find in N delegations

trust(a).
trust(b).
trust(c).
delegate(c,d).
delegate(d,e).
trust(X):- 3 > trust(X).
% find in N delegations

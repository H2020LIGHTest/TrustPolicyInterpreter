trust(b).
delegate(a,b).
trust(a):-delegate(a,b),trust(b).


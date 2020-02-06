isMember(X,cons(X,L)).
isMember(X,cons(Y,L)) :- isMember(X,L).

doAppend(empty,L,L).
doAppend(cons(X,L),M,cons(X,N)) :- doAppend(L,M,N).

getde(cons(d,cons(e,empty))).
getabc(cons(a,cons(b,cons(c,empty)))).
getdeabc(cons(d,cons(e,X))) :- getabc(X).

test() :- getde(DE), getabc(ABC), doAppend(DE,ABC,DEABC), getdeabc(DEABC).

failtest() :- getde(DE), doAppend(DE,ABC,ABC), getabc(ABC).
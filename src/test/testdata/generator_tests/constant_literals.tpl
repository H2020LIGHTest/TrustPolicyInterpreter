id(X,X).

accept(Form) :-
  extract(Form, format, 'TheAuctionHouse2019Format'),
  extract(Form, bid, Bid), 
  Bid <= 100,
  extract(Form, 'ConstLit', ConstLit),
  extract(Form, constLit, ConstLit2),
  id(ConstLit, ConstLit2).


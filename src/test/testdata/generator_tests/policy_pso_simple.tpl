accept(Form) :-
  extract(Form, format, pumpkinSeedOil),
  authorize_order(Form).


authorize_order(Form) :-
    extract(Form, item_id, 42),
    extract(Form, ammount, Ammount),
     Ammount <= 2.


authorize_order(Form) :-
    extract(Form, item_id, 54678),
    extract(Form, ammount, Ammount),
     Ammount <= 10.


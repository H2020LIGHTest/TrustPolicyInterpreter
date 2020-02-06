	accept(Form) :-
	  extract(Form, format, theAuctionHouse2019Format),
	  extract(Form, bid, Bid), 
	  Bid <= 100.
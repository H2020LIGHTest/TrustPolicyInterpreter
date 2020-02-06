document_format(auctionHouseDTU).
document_format(auctionHouseTUG).

accept(Document) :-
  document_format(Format),
  extract(Document, format, Format),
  extract(Document, bid, Bid),
  Bid <= 5.


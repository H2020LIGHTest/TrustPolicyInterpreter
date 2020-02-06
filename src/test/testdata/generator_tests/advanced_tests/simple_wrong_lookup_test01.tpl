main(Input) :- extract(Input, transaction, Transaction),
                extract(Transaction, trustList, Claim),
                lookup(Claim, trustListEntry).
main(Input) :- extract(Input, transaction, Transaction),
                extract(Transaction, trustList, Claim),
                trustscheme(Claim, IDAS).
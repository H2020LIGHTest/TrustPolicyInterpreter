main(Input) :- extract(Input, transaction, Transaction),
                extract(Transaction, trustList, Claim),
                trustscheme(claim, eIDAS_qualified).
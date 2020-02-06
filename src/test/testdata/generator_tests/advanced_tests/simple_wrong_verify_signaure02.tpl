main(Input) :- extract(Input, transaction, Transaction),
                extract(Transaction, document, Document),
                extract(Transaction, issuerKey, PkIssuer),
                verify_signature(Document, pkIssuer).
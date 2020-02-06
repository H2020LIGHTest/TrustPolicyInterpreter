accept(Input) :-
  extract(Input, document, Document),
  extract(Input, mandate, Mandate),
  checkQualifiedDelegation(Document, Mandate).

checkQualifiedDelegation(Document, Mandate) :-
	checkMandate(Document, Mandate),
	checkMandatorKey(Document, Mandate),
	checkValidDelegation(Document, Mandate),
	extract(Document, bid, BID), BID <= 1000.

checkMandate(Document, Mandate) :-
	extract(Mandate, format, delegation),
	extract(Mandate, proxyKey, PkSig),
	verify_signature(Document, PkSig),
	extract(Mandate, purpose, place_bid).

checkMandatorKey(Document, Mandate) :-
	extract(Mandate, issuer, MandatorCert),
	extract(MandatorCert, trustScheme, TrustSchemeClaim),
	trustscheme(TrustSchemeClaim, eIDAS_qualified),
	trustlist(TrustSchemeClaim, MandatorCert, TrustListEntry),
	extract(TrustListEntry, pubKey, PkIss),
	verify_signature(MandatorCert, PkIss).

checkValidDelegation(Document, Mandate) :-
	extract(Mandate, delegationProvider, DP),
	lookup(DP, DPEntry),
	extract(DPEntry, fingerprint, HMandate),
	verify_hash(Mandate, HMandate).
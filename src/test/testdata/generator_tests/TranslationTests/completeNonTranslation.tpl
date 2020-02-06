accept(Transaction) :-
	extract(Transaction, format, auctionHouse2019),

	extract(Transaction, certificate, Certificate),
	extract(Certificate, format, x509cert),

	% did the cert really sign the transaction?
	extract(Certificate, pubKey, PK),
	verify_signature(Transaction, PK),

	extract(Certificate, issuer, IssuerCertificate),
	extract(IssuerCertificate, trustscheme, TrustschemeMembershipClaim),

	trustlist(TrustschemeMembershipClaim, IssuerCertificate, TrustListEntry),
	trustschemeX(TrustschemeMembershipClaim, azTrustscheme, TrustListEntry, TrustedTrustListEntry),

    extract(TrustedTrustListEntry, format, generic_trustlist_format),
	% some issuer checks based on AZ tuples:
	extract(TrustedTrustListEntry, serviceType, eIDqualified),
	extract(TrustedTrustListEntry, signedInPerson, true),

	extract(TrustedTrustListEntry, pubKey, PkIss),
	verify_signature(Certificate, PkIss).


trustschemeX(Claim, TrustedScheme, TrustListEntry, TrustListEntry) :-
	% true iff Claim == gov.az
	trustscheme(Claim, TrustedScheme).
	% no need to translate => input param = output param (?)


trustschemeX(Claim, TrustedScheme, TrustListEntry, TrustedTrustListEntry) :-
	% Claim = trust.eu
	% TrustedScheme = gov.az
	% TTAdomain = trust.eu._translations._trust.goz.az
	encode_translation_domain(Claim, TrustedScheme, TTAdomain),
	lookup(TTAdomain, TranslationEntry),

	% TranslationEntry = translation table in e.g. XML
	translate(TranslationEntry, TrustListEntry, TrustedTrustListEntry).



accept(Form) :-
  extract(Form, format, theAuctionHouse2019format),
  extract(Form, bid, Bid),  Bid <= 1500,
  extract(Form, certificate, Certificate),
  extract(Certificate, pubKey, PK),
  verify_signature(Form, PK),
  check_eIDAS_qualified(Certificate).

check_eIDAS_qualified(Certificate) :-
  extract(Certificate, format, eIDAS_qualified_certificate),
  extract(Certificate, issuer, IssuerCertificate),
  extract(IssuerCertificate, trustScheme, TrustSchemeClaim),

  trustscheme(TrustSchemeClaim, eIDAS_qualified),
  trustlist(TrustSchemeClaim, IssuerCertificate, TrustListEntry),

  extract(TrustListEntry, pubKey, PkIss),
  verify_signature(Certificate, PkIss).
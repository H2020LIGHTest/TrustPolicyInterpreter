check_valid_document(Document) :-
% Document is actual the root variable the (Pdf?) parser passes over the ATV to the interpreter
% Fields like ´seal´ are virtual fields (virtual since it is not exactly a real field like XMLs have),
% which are added during parsing (e.g. seal is extracted with some PAdES library from the pdf).

	extract(Document, format, correos_pdf_format),
	% Tells the interpreter that correos_pdf_format is accepted and
        % the further TPL code will extract from its specification
	extract(Document, seal, ESeal),
	% The seal is extracted from the root var `Document` and assigned to the var `ESeal`
        % (I would call it Certificate. ESeal implies somehow Signature...)
	extract(ESeal, format, certificate),
        %
	extract(ESeal, pubKey, SealPK),
	% The public key is extracted from the seal and assigned to the variable `SealPK`

	verify_signature(Document, SealPK),
	% is the document really signed by the certificate?

	extract(ESeal, issuer, IssuerCertificate),
        % who signed the signing Certificate

	trustscheme0(IssuerCertificate, eIDAS_qualified, TrustedTrustListEntry),
        % defined below. One for eIDAS, one for all the other. Add another for `explicitly` trusting another Trustscheme

	extract(TrustedTrustListEntry, pubKey, PkIss),
	verify_signature(ESeal, PkIss).
        % still would call ESeal -> certificate
        % you can add furhter checks regarding properties of the document if you want,
        % as long as your correos_pdf_format parser provides virtual fields for it.


trustscheme0(IssuerCert, TrustedScheme, TrustedTrustListEntry) :-
% with Translation: Turkey_qualified (in the simple case that Turkey has a boolean Trustscheme)

	extract(IssuerCert, trustscheme, Claim),
        % where does it claim to be valid (i.e in which trust scheme)?
	trustlist(Claim, IssuerCert, TrustListEntry),
        % communicating with Turkish TSPA
	encode_translation_domain(Claim, TrustedScheme, TTAdomain),
        % generating lookup domain
	lookup(TTAdomain, TranslationEntry),
	% getting TranslationEntry for translation into eIDAS (form TTA); if no translation it fails -> returns false
	translate(TranslationEntry, TrustListEntry, TrustedTrustListEntry).

trustscheme0(IssuerCert, TrustedScheme, TrustListEntry) :-
% without Translation: eIDAS_qualified

	extract(IssuerCert, trustscheme, Claim),
        % where does it claim to be valid (i.e in which trust scheme)?
	trustlist(Claim, IssuerCert, TrustListEntry),
        % communicating with EU TSPA
	trustscheme(Claim, TrustedScheme).
        % is it eIDAS?
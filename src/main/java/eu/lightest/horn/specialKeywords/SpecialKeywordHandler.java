package eu.lightest.horn.specialKeywords;

import eu.lightest.horn.AST.TplPredication;

public class SpecialKeywordHandler {
  public SpecialKeywordHandler() {
  }

  public SpecialKeyword detect(TplPredication query) {
    switch (query.mId) {
      case "print":
        return new Print(query);
      case "extract":
        return new Extract(query);
      case "verify_signature":
        return new VerifySignature(query);
      case "trustscheme":
        return new Trustscheme(query);
      case "lookup":
        return new Lookup(query);
      case "trustlist":
        return new TrustList(query);
      case "verify_hash":
        return new VerifyHash(query);
      case "translate":
        return new Translate(query);
      case "encode_translation_domain":
        return new EncodeTranslationDomain(query);
      default:
        return null;
    }
  }
}

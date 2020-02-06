package eu.lightest.horn.util;

import eu.lightest.horn.specialKeywords.HornApiException;
import eu.lightest.horn.specialKeywords.IAtvApiListener;

import java.util.*;

public class AtvApiDummy implements IAtvApiListener {
  private Map<List<String>,Entry> document;

  private class Entry {
    String type;
    Object val;

    Entry(String type, Object val) {
      this.type = type;
      this.val = val;
    }

    Entry(Integer n) {
      this("INT",n);
    }

    Entry(String s) {
      this("STRING",s);
    }

    @Override
    public String toString() {
      return "Entry(" + type + ", " + val + ")";
    }
  }

  public AtvApiDummy() {
    document = new HashMap<>();
  }

  @Override
  public boolean onExtract(List<String> input, String query, List<String> output) throws HornApiException {
    output.addAll(input);
    output.add(query);
    return true;
  }

  @Override
  public boolean onVerifySignature(List<String> subject, List<String> key) throws HornApiException {
    // TODO: implement
    return true;
  }

  @Override
  public boolean onVerifyHash(List<String> object, List<String> hash) throws HornApiException {
    // TODO: implement
    return true;
  }

  @Override
  public boolean onTrustschemeCheck(List<String> claim, String scheme) throws HornApiException {
    // TODO: implement
    return true;
  }

  @Override
  public boolean onLookup(List<String> domain, List<String> entry) throws HornApiException {
    // TODO: Why is the 'entry' parameter not modified?
    return true;
  }

  @Override
  public boolean onTrustlist(List<String> domain, List<String> cert, List<String> entry) throws HornApiException {
    entry.add("trustListEntry");
    // TODO: implement
    return true;
  }
  
  @Override
  public boolean onPrint(PrintObj printObj) {
    return true;
  }
  
  @Override
  public ResolvedObj resolveObj(List<String> mValue) {
    if (mValue == null || mValue.size() < 2) {
      return null;
    }

    Entry entry = document.get(mValue);

    return entry == null ? null : new ResolvedObj(entry.val, entry.type);
  }

  @Override
  public boolean setFormat(List<String> input, String format) {
    System.out.println("format is set to: <" + format + "> and input to: <" + input + ">");
    return true;
  }

  @Override
  public boolean onTranslate(List<String> translation_entry, List<String> trustlist_entry, List<String> trusted_trustlist_entry) throws HornApiException {
    trusted_trustlist_entry.addAll(trustlist_entry);
    //    TODO build Dummy
    return true;
  }

  @Override
  public boolean onEncodeTranslationDomain(List<String> claim, String trusted_scheme, List<String> translation_domain) throws HornApiException {
//    TODO build Dummy
    return true;
  }

  public AtvApiDummy addDocumentEntry(Integer n, String... path) {
    document.put(Arrays.asList(path), new Entry(n));
    return this;
  }

  public AtvApiDummy addDocumentEntry(String s, String... path) {
    document.put(Arrays.asList(path), new Entry(s));
    return this;
  }

}

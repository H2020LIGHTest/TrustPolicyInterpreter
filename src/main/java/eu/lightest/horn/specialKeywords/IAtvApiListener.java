package eu.lightest.horn.specialKeywords;

import java.util.List;

public interface IAtvApiListener {
  
  class ResolvedObj{
    public Object mValue;
    public String mType; //"INT","STRING","..." a cast needs to be implemented

    public ResolvedObj(Object val, String type) {
      this.mValue = val;
      this.mType = type;
    }
  }

  class PrintObj{
    public enum Type {Obj, Str};
    public Type mType;
    public List<String> mPath;
    public String mValue;

    public PrintObj(String value) {
      this.mType = Type.Str;
      this.mValue = value;
    }

    public PrintObj(List<String> path) {
      this.mType = Type.Obj;
      this.mPath = path;
    }
  }

  boolean onExtract(List<String> input, String query, List<String> output) throws HornApiException;

  boolean onVerifySignature(List<String>  subject, List<String>  key) throws HornApiException;
  boolean onVerifyHash(List<String> object, List<String> hash) throws HornApiException;

  boolean onTrustschemeCheck(List<String> claim, String scheme) throws HornApiException;

  boolean onLookup(List<String> domain, List<String> entry) throws HornApiException;
  boolean onTrustlist(List<String> domain, List<String> cert, List<String> entry) throws HornApiException;
  
  boolean onPrint(PrintObj printObj);
  
  ResolvedObj resolveObj(List<String> mValue);

  boolean setFormat(List<String> input, String format);

  boolean onTranslate(List<String> translation_entry, List<String> trustlist_entry, List<String> trusted_trustlist_entry) throws HornApiException;

  boolean onEncodeTranslationDomain(List<String> claim, String trusted_scheme, List<String> translation_domain) throws HornApiException;
}

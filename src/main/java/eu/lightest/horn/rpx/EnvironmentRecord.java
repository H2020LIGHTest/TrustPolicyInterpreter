package eu.lightest.horn.rpx;

import eu.lightest.horn.AST.*;
import eu.lightest.horn.exceptions.ProgrammingError;

import java.util.*;
import java.util.regex.Pattern;

public class EnvironmentRecord {

  final private List<TplExpression> query;
  final private List<TplClause> policy;
  final private List<TplClause> recordedClauses;
  final private List<TplTermObject> objects;

  private EnvironmentRecord(List<TplExpression> query, List<TplClause> policy, List<TplClause> recordedClauses, List<TplTermObject> objects){
    this.query = query;
    this.policy = policy;
    this.recordedClauses = recordedClauses;
    this.objects = objects;
  }

  public EnvironmentRecord(List<TplExpression> query, List<TplClause> policy){
    this(query,policy,new ArrayList<>(),new ArrayList<>());
  }

  public EnvironmentRecord recordComparison(TplRelop r){
    List<TplClause> recordedEvents2 = new ArrayList<>(recordedClauses);
    TplClause c = new TplClause();
    c.mHead = r;
    recordedEvents2.add(c);
    return new EnvironmentRecord(query,policy,recordedEvents2, objects);
  }

  public EnvironmentRecord recordObjects(List<TplTermObject> os){
    List<TplTermObject> objects2 = new ArrayList<>(objects);
    objects2.addAll(os);
    return new EnvironmentRecord(query,policy, recordedClauses, objects2);
  }

  public EnvironmentRecord recordClause(TplClause c){
    List<TplClause> recordedEvents2 = new ArrayList<>(recordedClauses);
    recordedEvents2.add(c);
    return new EnvironmentRecord(query,policy,recordedEvents2, objects);
  }

  public EnvironmentRecord recordClause(TplExpression head, TplExpression body) {
    if(head.toString().equals(body.toString())){
      return this;
    }
    TplClause evaluationRecord = new TplClause();
    evaluationRecord.mHead = head;
    evaluationRecord.mBody.add(body);
    return recordClause(evaluationRecord);
  }

  public EnvironmentRecord recordFact(TplPredication f){
    TplClause c = new TplClause();
    c.mHead = f;
    return recordClause(c);
  }

  public String toTPTP() {
    //Create set of function symbols
    Set<String> funs = new HashSet<>();
    for(TplExpression e : query){
      funs.addAll(e.getFunctionSymbols());
    }
    for(TplClause c : policy){
      funs.addAll(c.getFunctionSymbols());
    }
    for(TplClause c : recordedClauses){
      funs.addAll(c.getFunctionSymbols());
    }
    for(TplTermObject o : objects){
      Object obj = o.resolventForTPTPTranslation();
      if (obj instanceof String) {
        funs.add((String) obj);
      }
    }
    //Create set of variable symbols
    Set<String> vars = new HashSet<>();
    for(TplExpression e : query){
      vars.addAll(e.getVariables());
    }
    for(TplClause c : policy){
      vars.addAll(c.getVariables());
    }
    for(TplClause c : recordedClauses){
      vars.addAll(c.getVariables());
    }

    //Create function symbol translation
    Map<String, String> funTranslation = new HashMap<>();
    //Create map for the functions that do not map to themselves
    Map<String, String> funIds = new HashMap<>();
    int funId = 0;
    for(String f : funs){
      if (isTPTP(f)){
        funTranslation.put(f,"term_" + f);
      } else {
        funTranslation.put(f,"funid_" + funId);
        funIds.put(f,"funid_" + funId);
        funId++;
      }
    }

    //Create variable translation
    Map<String, String> varTranslation = new HashMap<>();
    //Create map for the variables that do not map to themselves
    Map<String, String> varIds = new HashMap<>();
    int varId = 0;
    for(String v : vars){
      if (isTPTPVar(v)){
        varTranslation.put(v,"Var_" + v);
      } else {
        varTranslation.put(v,"Varid_" + varId);
        varIds.put(v,"Varid_" + varId);
        varId++;
      }
    }

    //Create path translation
    Map<List<String>, String> pathTranslation = new HashMap<>();
    //Create map for the paths that do not map to themselves
    Map<List<String>, String> pathIds = new HashMap<>();
    int pathId = 0;
    for (TplTermObject o : objects) {
      if (o.resolventForTPTPTranslation() == null) {
        if (isTPTPPath(o.mValue)){
          pathTranslation.put(o.mValue, encodePath(o.mValue));
        } else {
          pathTranslation.put(o.mValue,"pathid_" + pathId);
          pathIds.put(o.mValue,"pathid_" + pathId);
          pathId++;
        }
      }
    }
    for (TplTermObject o : objects) {
      Object obj = o.resolventForTPTPTranslation();
      if (obj != null) {
        if (obj instanceof Integer){
          pathTranslation.put(o.mValue,encodeInt((int) obj));
        } else if (obj instanceof String){
          pathTranslation.put(o.mValue, encodeCst(funTranslation,(String) obj));
        }
      }
    }

    String tptp = "";
    tptp += toTPTP(query,varTranslation,funTranslation,pathTranslation);

    for(TplClause c : policy){
      tptp += toTPTP(c,varTranslation,funTranslation,pathTranslation);
    }
    for(TplClause c : recordedClauses){
      tptp += toTPTP(c,varTranslation,funTranslation,pathTranslation);
    }

    if(!funIds.isEmpty() || !varIds.isEmpty() || !pathIds.isEmpty()){
      tptp += "\n\n";
      for (Map.Entry<String, String> entry : funIds.entrySet()) {
        tptp += "%" + entry.getValue() + "=\n";
        tptp += commentindent(entry.getKey());
      }
      for (Map.Entry<String, String> entry : varIds.entrySet()) {
        tptp += "%" + entry.getValue() + "=\n";
        tptp += commentindent(entry.getKey());
      }
      for (Map.Entry<List<String>, String> entry : pathIds.entrySet()) {
        tptp += "%" + entry.getValue() + "=\n";
        tptp += commentindentpath(entry.getKey());
      }
    }

    return tptp;
  }

  private String encodeCst(Map<String, String> funTranslation, String id) {
    return encodeFun(funTranslation, id, 0);
  }

  private String encodeFun(Map<String, String> funTranslation, String id, int i) {
    return encodeFunRaw(funTranslation.get(id),i);
  }

  private String encodeFunRaw(String s, int i) {
    return s + "_" + i;
  }

  private String encodeInt(int i) {
    if (i < 0){
      return "negint_" + ("" + i).replace("-","");
    }
    return encodeFunRaw("term_"+ i, 0);
  }

  private String encodePath(List<String> path) {
    return "obj_" + String.join("_", path);
  }

  private boolean isTPTPPath(List<String> mValue) {
    if(mValue == null){
      return false;
    }
    for(String s : mValue){
      if (s==null){
        return false;
      }
      if (!isTPTP(s)){
        return false;
      }
    }
    return true;
  }

  private String commentindent(String f) {
    return "%    " + f.replace("\n","\n%    ").replace("\r","\r%    ");
  }

  private String commentindentpath(List<String> path) {
    List<String> tptps = new ArrayList<>();
    for(String s : path){
      tptps.add(commentindent(s));
    }
    return String.join("%  .\n", tptps);
  }

  private boolean isTPTP(String f) {
    return Pattern.compile("[a-zA-Z0-9_]+").matcher(f).matches();
  }

  private boolean isTPTPVar(String v) {
    return Pattern.compile("[A-Z][a-zA-Z0-9_]*").matcher(v).matches();
  }

  private String toTPTP(TplClause c, Map<String,String> varTranslation, Map<String,String> funTranslation, Map<List<String>,String> pathTranslation) {
    String tptp = "cnf(tpl,axiom,(" + toTPTP(c.mHead, varTranslation, funTranslation, pathTranslation);

    if (c.mBody.size() > 0) {
      for (TplExpression f : c.mBody) {
        tptp += "| ~";
        tptp += toTPTP(f, varTranslation, funTranslation, pathTranslation);
      }
    }

    tptp += ")).\n";

    return tptp;
  }

  private String toTPTP(TplExpression e, Map<String,String> varTranslation, Map<String,String> funTranslation, Map<List<String>,String> pathTranslation) {
    String tptp;
    if (e instanceof TplRelop) {
      tptp = relOpToString(((TplRelop) e).mOp);
    } else if (e instanceof TplPredication) {
      tptp = "pred_" + ((TplPredication) e).mId + "_" + e.mTerms.size();
    } else {
      throw new ProgrammingError("Case not covered.");
    }
    if (e.mTerms.size() > 0) {
      List<String> args = new ArrayList<>();
      for (TplTerm u : e.mTerms) {
        args.add(toTPTP(u, varTranslation, funTranslation, pathTranslation));
      }
      tptp += "(" + String.join(", ", args) +")";
    }
    return tptp;
  }

  private String toTPTP(TplTerm t, Map<String,String> varTranslation, Map<String,String> funTranslation, Map<List<String>,String> pathTranslation) {
    if (t instanceof TplTermArith){
      List<String> args = new ArrayList<>();
      for(TplTerm s : ((TplTermArith) t).mTerms){
        args.add(toTPTP(s,varTranslation,funTranslation, pathTranslation));
      }
      return arithToString(((TplTermArith) t).mOp) + "(" + String.join(",", args) + ")";
    } else if (t instanceof TplTermComposite){
      TplTermComposite tc = (TplTermComposite) t;
      String tptp = encodeFun(funTranslation,tc.getId(),tc.mTerms.size());
      if (tc.mTerms.size() > 0) {
        List<String> args = new ArrayList<>();
        for (TplTerm s : tc.mTerms) {
          args.add(toTPTP(s, varTranslation, funTranslation, pathTranslation));
        }
        tptp += "(" + String.join(", ", args) + ")";
      }
      return tptp;
    } else if(t instanceof TplTermInteger){
      return encodeInt(((TplTermInteger) t).mValue);
    } else if(t instanceof TplTermObject){
      return pathTranslation.get(((TplTermObject) t).getValue());
    } else if(t instanceof TplTermVar){
      return varTranslation.get(((TplTermVar) t).mId);
    }
    throw new ProgrammingError("Term not covered: " + t);
  }

  private String arithToString(String op) {
    switch (op) {
      case "-":
        return "arith_minus";
      case "+":
        return "arith_plus";
      case "*":
        return "arith_mult";
      default:
        throw new ProgrammingError("no other arith allowed!");
    }
  }

  private String relOpToString(String op){
    switch (op) {
      case "<":
        return "op_ls";
      case ">":
        return "op_gr";
      case "==":
        return "op_eq";
      case "<=":
        return "op_lseq";
      case ">=":
        return "op_greq";
      default:
        throw new ProgrammingError("no such thing for relop");
    }
  }

  private String toTPTP(List<TplExpression> q, Map<String,String> varTranslation, Map<String,String> funTranslation, Map<List<String>,String> pathTranslation) {
    String tptp = "cnf(tpl,negated_conjecture, ~" + toTPTP(q.get(0), varTranslation, funTranslation, pathTranslation);
    for (int i = 1; i < q.size(); i++) {
      tptp += "| ~" + toTPTP(q.get(i), varTranslation, funTranslation, pathTranslation);
    }
    return tptp + ").\n";
  }
}

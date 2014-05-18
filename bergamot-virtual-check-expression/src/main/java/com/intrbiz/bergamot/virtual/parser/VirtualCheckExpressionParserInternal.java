/* Generated By:JavaCC: Do not edit this line. VirtualCheckExpressionParserInternal.java */
package com.intrbiz.bergamot.virtual.parser;

import com.intrbiz.bergamot.model.*;
import com.intrbiz.bergamot.model.virtual.*;
import com.intrbiz.bergamot.virtual.*;
import java.util.*;

@SuppressWarnings("all")
public final class VirtualCheckExpressionParserInternal implements VirtualCheckExpressionParserInternalConstants {

  final private UUID readUUID() throws ParseException {
    Token t;
    t = jj_consume_token(LUUID);
      {if (true) return UUID.fromString(t.image);}
    throw new Error("Missing return statement in function");
  }

  final private String readString() throws ParseException {
    Token t;
    t = jj_consume_token(LSTRING);
      {if (true) return t.image.substring(1, t.image.length() -1);}
    throw new Error("Missing return statement in function");
  }

  final private ValueOperator readHost(VirtualCheckExpressionParserContext c) throws ParseException {
    String name = null;
    UUID id = null;
    Host host = null;
    jj_consume_token(HOST);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LUUID:
      id = readUUID();
                  host = c.lookupHost(id);
      break;
    case LSTRING:
      name = readString();
                  host = c.lookupHost(name);
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return new ValueOperator(host);}
    throw new Error("Missing return statement in function");
  }

  final private ValueOperator readService(VirtualCheckExpressionParserContext c) throws ParseException {
    String name = null;
    String hostName = null;
    UUID id = null;
    Service service = null;
    Host host = null;
    jj_consume_token(SERVICE);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LUUID:
      id = readUUID();
                  service = c.lookupService(id);
      break;
    case LSTRING:
      name = readString();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LON:
        jj_consume_token(LON);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case HOST:
          jj_consume_token(HOST);
          break;
        default:
          jj_la1[1] = jj_gen;
          ;
        }
        hostName = readString();
                      host = c.lookupHost(hostName);
                      service = c.lookupService(host, name);
        break;
      default:
        jj_la1[2] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return new ValueOperator(service);}
    throw new Error("Missing return statement in function");
  }

  final private ValueOperator readTrap(VirtualCheckExpressionParserContext c) throws ParseException {
    String name = null;
    String hostName = null;
    UUID id;
    Trap trap = null;
    Host host = null;
    jj_consume_token(TRAP);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LUUID:
      id = readUUID();
                  trap = c.lookupTrap(id);
      break;
    case LSTRING:
      name = readString();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LON:
        jj_consume_token(LON);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case HOST:
          jj_consume_token(HOST);
          break;
        default:
          jj_la1[4] = jj_gen;
          ;
        }
        hostName = readString();
                      host = c.lookupHost(hostName);
                      trap = c.lookupTrap(host, name);
        break;
      default:
        jj_la1[5] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return new ValueOperator(trap);}
    throw new Error("Missing return statement in function");
  }

  final private ValueOperator readCluster(VirtualCheckExpressionParserContext c) throws ParseException {
    String name = null;
    UUID id = null;
    Cluster cluster = null;
    jj_consume_token(CLUSTER);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LUUID:
      id = readUUID();
                  cluster = c.lookupCluster(id);
      break;
    case LSTRING:
      name = readString();
                  cluster = c.lookupCluster(name);
      break;
    default:
      jj_la1[7] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return new ValueOperator(cluster);}
    throw new Error("Missing return statement in function");
  }

  final private ValueOperator readResource(VirtualCheckExpressionParserContext c) throws ParseException {
    String name = null;
    String clusterName = null;
    UUID id = null;
    Cluster cluster = null;
    Resource resource = null;
    jj_consume_token(RESOURCE);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LUUID:
      id = readUUID();
                  resource = c.lookupResource(id);
      break;
    case LSTRING:
      name = readString();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LON:
        jj_consume_token(LON);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case CLUSTER:
          jj_consume_token(CLUSTER);
          break;
        default:
          jj_la1[8] = jj_gen;
          ;
        }
        clusterName = readString();
                      cluster = c.lookupCluster(clusterName);
                      resource = c.lookupResource(cluster, name);
        break;
      default:
        jj_la1[9] = jj_gen;
        ;
      }
      break;
    default:
      jj_la1[10] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return new ValueOperator(null);}
    throw new Error("Missing return statement in function");
  }

  final private ValueOperator readCheck(VirtualCheckExpressionParserContext c) throws ParseException {
    ValueOperator check;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case HOST:
      check = readHost(c);
      break;
    case SERVICE:
      check = readService(c);
      break;
    case TRAP:
      check = readTrap(c);
      break;
    case CLUSTER:
      check = readCluster(c);
      break;
    case RESOURCE:
      check = readResource(c);
      break;
    default:
      jj_la1[11] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return check;}
    throw new Error("Missing return statement in function");
  }

  final private VirtualCheckOperator readValue(VirtualCheckExpressionParserContext c) throws ParseException {
    VirtualCheckOperator value;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case STCOMP:
      jj_consume_token(STCOMP);
      value = readExpression(c);
      jj_consume_token(EDCOMP);
              value = new BracketOperator(value);
      break;
    default:
      jj_la1[12] = jj_gen;
      if (jj_2_1(2147483647)) {
        value = readCheck(c);
      } else {
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
      {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  final private VirtualCheckOperator readUnary(VirtualCheckExpressionParserContext c) throws ParseException {
    VirtualCheckOperator value;
    jj_consume_token(NOT);
    value = readValue(c);
      {if (true) return new NotOperator(value);}
    throw new Error("Missing return statement in function");
  }

  final private VirtualCheckOperator readOperator(VirtualCheckExpressionParserContext c) throws ParseException {
    VirtualCheckOperator operator;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case STCOMP:
    case HOST:
    case SERVICE:
    case TRAP:
    case RESOURCE:
    case CLUSTER:
      operator = readValue(c);
      break;
    case NOT:
      operator = readUnary(c);
      break;
    default:
      jj_la1[13] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return operator;}
    throw new Error("Missing return statement in function");
  }

  final public VirtualCheckOperator readExpression(VirtualCheckExpressionParserContext c) throws ParseException {
    VirtualCheckOperator operator;
    operator = readXorExpression(c);
      {if (true) return operator;}
    throw new Error("Missing return statement in function");
  }

  final private VirtualCheckOperator readXorExpression(VirtualCheckExpressionParserContext c) throws ParseException {
    VirtualCheckOperator l;
    VirtualCheckOperator r;
    l = readOrExpression(c);
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case XOR:
        ;
        break;
      default:
        jj_la1[14] = jj_gen;
        break label_1;
      }
      jj_consume_token(XOR);
      r = readOrExpression(c);
              l = new XorOperator(l, r);
    }
      {if (true) return l;}
    throw new Error("Missing return statement in function");
  }

  final private VirtualCheckOperator readOrExpression(VirtualCheckExpressionParserContext c) throws ParseException {
    VirtualCheckOperator l;
    VirtualCheckOperator r;
    l = readAndExpression(c);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        ;
        break;
      default:
        jj_la1[15] = jj_gen;
        break label_2;
      }
      jj_consume_token(OR);
      r = readAndExpression(c);
              l = new OrOperator(l, r);
    }
      {if (true) return l;}
    throw new Error("Missing return statement in function");
  }

  final private VirtualCheckOperator readAndExpression(VirtualCheckExpressionParserContext c) throws ParseException {
    VirtualCheckOperator l;
    VirtualCheckOperator r;
    l = readOperator(c);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        ;
        break;
      default:
        jj_la1[16] = jj_gen;
        break label_3;
      }
      jj_consume_token(AND);
      r = readOperator(c);
             l = new AndOperator(l,r);
    }
      {if (true) return l;}
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_3R_15() {
    if (jj_3R_25()) return true;
    return false;
  }

  private boolean jj_3R_12() {
    if (jj_scan_token(TRAP)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_19()) {
    jj_scanpos = xsp;
    if (jj_3R_20()) return true;
    }
    return false;
  }

  private boolean jj_3R_22() {
    if (jj_3R_26()) return true;
    return false;
  }

  private boolean jj_3R_9() {
    if (jj_3R_14()) return true;
    return false;
  }

  private boolean jj_3R_27() {
    if (jj_scan_token(LON)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(13)) jj_scanpos = xsp;
    if (jj_3R_26()) return true;
    return false;
  }

  private boolean jj_3R_8() {
    if (jj_3R_13()) return true;
    return false;
  }

  private boolean jj_3R_7() {
    if (jj_3R_12()) return true;
    return false;
  }

  private boolean jj_3R_21() {
    if (jj_3R_25()) return true;
    return false;
  }

  private boolean jj_3R_6() {
    if (jj_3R_11()) return true;
    return false;
  }

  private boolean jj_3R_5() {
    if (jj_3R_10()) return true;
    return false;
  }

  private boolean jj_3R_10() {
    if (jj_scan_token(HOST)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_15()) {
    jj_scanpos = xsp;
    if (jj_3R_16()) return true;
    }
    return false;
  }

  private boolean jj_3R_4() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_5()) {
    jj_scanpos = xsp;
    if (jj_3R_6()) {
    jj_scanpos = xsp;
    if (jj_3R_7()) {
    jj_scanpos = xsp;
    if (jj_3R_8()) {
    jj_scanpos = xsp;
    if (jj_3R_9()) return true;
    }
    }
    }
    }
    return false;
  }

  private boolean jj_3R_29() {
    if (jj_scan_token(LON)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(17)) jj_scanpos = xsp;
    if (jj_3R_26()) return true;
    return false;
  }

  private boolean jj_3R_18() {
    if (jj_3R_26()) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_27()) jj_scanpos = xsp;
    return false;
  }

  private boolean jj_3R_13() {
    if (jj_scan_token(CLUSTER)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_21()) {
    jj_scanpos = xsp;
    if (jj_3R_22()) return true;
    }
    return false;
  }

  private boolean jj_3R_17() {
    if (jj_3R_25()) return true;
    return false;
  }

  private boolean jj_3R_28() {
    if (jj_scan_token(LON)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(13)) jj_scanpos = xsp;
    if (jj_3R_26()) return true;
    return false;
  }

  private boolean jj_3R_26() {
    if (jj_scan_token(LSTRING)) return true;
    return false;
  }

  private boolean jj_3_1() {
    if (jj_3R_4()) return true;
    return false;
  }

  private boolean jj_3R_24() {
    if (jj_3R_26()) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_29()) jj_scanpos = xsp;
    return false;
  }

  private boolean jj_3R_23() {
    if (jj_3R_25()) return true;
    return false;
  }

  private boolean jj_3R_25() {
    if (jj_scan_token(LUUID)) return true;
    return false;
  }

  private boolean jj_3R_11() {
    if (jj_scan_token(SERVICE)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_17()) {
    jj_scanpos = xsp;
    if (jj_3R_18()) return true;
    }
    return false;
  }

  private boolean jj_3R_20() {
    if (jj_3R_26()) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_28()) jj_scanpos = xsp;
    return false;
  }

  private boolean jj_3R_19() {
    if (jj_3R_25()) return true;
    return false;
  }

  private boolean jj_3R_14() {
    if (jj_scan_token(RESOURCE)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_23()) {
    jj_scanpos = xsp;
    if (jj_3R_24()) return true;
    }
    return false;
  }

  private boolean jj_3R_16() {
    if (jj_3R_26()) return true;
    return false;
  }

  /** Generated Token Manager. */
  public VirtualCheckExpressionParserInternalTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[17];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x800040,0x2000,0x40000,0x800040,0x2000,0x40000,0x800040,0x800040,0x20000,0x40000,0x800040,0x3e000,0x800,0x3ec00,0x80,0x100,0x200,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[1];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public VirtualCheckExpressionParserInternal(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public VirtualCheckExpressionParserInternal(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new VirtualCheckExpressionParserInternalTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public VirtualCheckExpressionParserInternal(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new VirtualCheckExpressionParserInternalTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public VirtualCheckExpressionParserInternal(VirtualCheckExpressionParserInternalTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(VirtualCheckExpressionParserInternalTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[32];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 17; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 32; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 1; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}

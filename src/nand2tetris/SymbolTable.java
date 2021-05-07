package nand2tetris;

import java.util.HashMap;
import java.util.Map;

import static nand2tetris.VarAttributionType.*;

public class SymbolTable {

  private Map<String,SymbolValueBean> globalTable;
  private Map<String,SymbolValueBean> localTable;
  private int g_staticIndex;
  private int g_fieldIndex;
  private int l_argIndex;
  private int l_varIndex;

  SymbolTable() {
    this.globalTable = new HashMap<>();
    this.localTable = new HashMap<>();
    this.g_staticIndex=0;
    this.g_fieldIndex=0;
    this.l_argIndex=0;
    this.l_varIndex=0;
  }

  public void startSubroutine (){
    this.localTable = new HashMap<>();
    this.l_argIndex=0;
    this.l_varIndex=0;
  }

  public void define (String name,String type, VarAttributionType kind) throws Exception {
    switch (kind) {
      case STATIC:
        this.globalTable.put(name,new SymbolValueBean(name,type,kind,this.g_staticIndex));
        this.g_staticIndex+=1;
        break;
      case FIELD:
        this.globalTable.put(name,new SymbolValueBean(name,type,kind,this.g_fieldIndex));
        this.g_fieldIndex+=1;
        break;
      case ARG:
        this.localTable.put(name,new SymbolValueBean(name,type,kind,this.l_argIndex));
        this.l_argIndex+=1;
        break;
      case VAR:
        this.localTable.put(name,new SymbolValueBean(name,type,kind,this.l_varIndex));
        this.l_varIndex+=1;
        break;
      case NONE:
      default:
        throw new Exception();
    }
  }

  public int varCount (VarAttributionType kind) throws Exception {
    int ret=0;
    switch (kind) {
      case STATIC:
        ret=this.g_staticIndex;
        break;
      case FIELD:
        ret=this.g_fieldIndex;
        break;
      case ARG:
        ret=this.l_argIndex;
        break;
      case VAR:
        ret=this.l_varIndex;
        break;
      case NONE:
      default:
        throw new Exception();
    }
    return ret;
  }

  public VarAttributionType kindOf (String name) throws Exception {
    if (this.globalTable.containsKey(name)) {
      return this.globalTable.get(name).getKind();
    }
    if (this.localTable.containsKey(name)) {
      return this.localTable.get(name).getKind();
    }
    throw new Exception();
  }

  public String typeOf (String name) throws Exception {
    if (this.globalTable.containsKey(name)) {
      return this.globalTable.get(name).getType();
    }
    if (this.localTable.containsKey(name)) {
      return this.localTable.get(name).getType();
    }
    throw new Exception();
  }

  public int indexOf (String name) throws Exception {
    if (this.globalTable.containsKey(name)) {
      return this.globalTable.get(name).getIndex();
    }
    if (localTable.containsKey(name)) {
      return this.localTable.get(name).getIndex();
    }
    throw new Exception();
  }

}

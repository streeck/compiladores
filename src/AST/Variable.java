/* ------------------------------
Charles David de Moraes RA: 489662
Vitor Kusiaki             RA: 408140
------------------------------ */

package AST;

public class Variable {
  final private Type type;
  final private String identifier;

  public Variable(Type type, String identifier) {
    this.type = type;
    this.identifier = identifier;
  }

  public Type getType() {
    return type;
  }
}

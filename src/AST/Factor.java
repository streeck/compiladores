/* ------------------------------
Charles David de Moraes RA: 489662
Vitor Kusiaki             RA: 408140
------------------------------ */

package AST;

abstract public class Factor {
  protected LValue lvalue;

  public Factor(LValue lvalue) {
    this.lvalue = lvalue;
  }
}

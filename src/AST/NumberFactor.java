/* ------------------------------
Charles David de Moraes RA: 489662
Vitor Kusiaki             RA: 408140
------------------------------ */

package AST;

public class NumberFactor extends Factor {
  private final Numberino number;

  public NumberFactor(LValue lvalue, Numberino number) {
    super(lvalue);
    this.number = number;
  }
}

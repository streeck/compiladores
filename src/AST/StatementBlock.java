/* ------------------------------
Charles David de Moraes RA: 489662
Vitor Kusiaki             RA: 408140
------------------------------ */

package AST;
import java.util.ArrayList;

public class StatementBlock {

  final private ArrayList<Variable> vars;
  final private ArrayList<Statement> statement;

  public StatementBlock(ArrayList<Variable> vars, ArrayList<Statement> statement) {
    this.vars = vars;
    this.statement = statement;
  }
}

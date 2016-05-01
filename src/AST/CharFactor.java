package AST;

public class CharFactor extends Factor {
  private final Character c;

    public CharFactor(LValue lvalue, Character c) {
        super(lvalue);
        this.c = c;
    }
}

package lox;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    /**
     * Constructor for Token.
     * @param type Type of token as TokenType.
     * @param lexeme
     * @param literal
     * @param line Line number of token as String.
     */
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    /**
     * Returns string representation of token.
     * @return type, lexeme, and literal as String.
     */
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}

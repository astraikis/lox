package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;
    private int start = 0;
    private int current = 0;
    private int line = 1;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    /**
     * Constructor for Scanner.
     * @param source Source code as String.
     */
    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // At beginning of next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * Get next token and add to tokens list.
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // Rest of line is a comment.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    /**
     * Gets next identifier or keyword and adds to
     * token list.
     */
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;

        addToken(type);
    }

    /**
     * Gets next number and adds to token list.
     */
    private void number() {
        // Consume all digits.
        while (isDigit(peek())) advance();

        // Look for decimal.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume decimal point.
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /**
     * Gets next string and adds to token list.
     */
    private void string() {
        // Consume all characters in string.
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        // Source code ended before closing quotation mark.
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // Consume closing quotation mark.
        advance();

        // Trim quotation marks and add to token list.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * Consumes and returns next character in source if it
     * matches expected.
     * @param expected Expected character as char.
     * @return true if next character matches expected, false
     * if not.
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    /**
     * Returns the next character in source but does not
     * consume it.
     * @return Next character in source.
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * Returns the character after the next character in
     * source but does not consume it.
     * @return Character after next character in source.
     */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    /**
     * Returns true if c is a-z, A-Z, or _, false otherwise.
     * @param c Character to check as char.
     * @return true if c is a-z, A-Z, or _, false otherwise.
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    /**
     * Returns true if c is a-z, A-Z, _, or 0-9, false
     * otherwise.
     * @param c Character to check as char.
     * @return true if c is a-z, A-Z, _, or 0-9, false otherwise.
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Returns true if c is a digit 0-9, false otherwise.
     * @param c Character to check as char.
     * @return true if c is a digit 0-9, false otherwise.
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Checks if scanner is at the end of the sourcecode file.
     * @return true if at end of source code file, false otherwise.
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Consume and return next character in source.
     * @return Next character in source as char.
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * Wrapper for addToken(TokenType type, Object literal).
     * @param type
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Create token for current lexeme and add to
     * tokens list.
     * @param type Token type as TokenType.
     * @param literal
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}

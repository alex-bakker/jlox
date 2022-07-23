package com.bakker.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bakker.lox.TokenType.*;


class Scanner {

    private static final Map<String, TokenType> keywords;

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
        keywords.put("read", READ);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;      // First char in cur lexeme
    private int current = 0;    // Cur char in cur lexeme
    private int line = 1;


    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }


    private void scanToken() {
        char c = advance();
        switch (c) {
            case '?': addToken(QUESTION); break;
            case ':': addToken(COLON); break;
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
                break;
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
                if(match('/')) {
                    while (peek() != '\n' && !isAtEnd())
                        advance();  // Use peek here to avoid eating new line so we can hit it below and do `line++`
                } else if (match('*')) {
                    blockComment();
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
                if(isDigit(c)) {
                    number();
                } else if(isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character: " + c); //TODO: A string of bad chars would log each char
                }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }


    private void number() {
        while(isDigit(peek())) advance();

        if(peek() == '.' && isDigit(peekNext())) {
            advance();

            while(isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // TODO: Maybe support escape codes or some fun string functionality?
    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) Lox.error(line, "Unterminated string.");

        advance(); // The other '"'

        String value = source.substring(start+1, current-1);
        addToken(STRING, value);
    }

    private void blockComment() {
        while(peek() != '*' && peekNext() != '/' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) Lox.error(line, "Incomplete block comment");
        advance();
        advance();
    }

    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean match(char expected) {
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    private void addToken(TokenType tokenType, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(tokenType, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
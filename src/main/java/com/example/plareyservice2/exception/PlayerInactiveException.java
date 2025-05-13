package com.example.plareyservice2.exception;

public class PlayerInactiveException extends RuntimeException {
    public PlayerInactiveException(String message) {
        super(message);
    }
}

package com.lightbend.lagom.recipes.cbpanel.impl.repository;

public class DatabaseDownException extends RuntimeException {
    public DatabaseDownException(String message) {
        super(message);
    }
}

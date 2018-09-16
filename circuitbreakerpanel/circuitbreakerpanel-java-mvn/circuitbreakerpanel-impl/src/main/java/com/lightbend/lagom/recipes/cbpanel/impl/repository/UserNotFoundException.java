package com.lightbend.lagom.recipes.cbpanel.impl.repository;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

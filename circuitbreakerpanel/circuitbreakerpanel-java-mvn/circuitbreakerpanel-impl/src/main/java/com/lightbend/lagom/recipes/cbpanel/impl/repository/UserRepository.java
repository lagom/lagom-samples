package com.lightbend.lagom.recipes.cbpanel.impl.repository;

import com.lightbend.lagom.recipes.cbpanel.api.User;

import java.util.concurrent.CompletableFuture;

public class UserRepository {
    
    private static final String STANDARD_LOCATION = "unknown";
    
    public CompletableFuture<User> getUser(Integer userId) {
        
        if (userId < 100 && userId > 0) {
            return CompletableFuture
                    .completedFuture(new User(String.format("User-%d", userId), userId, STANDARD_LOCATION));
        
        } else if (userId >= 100) {
            
            throw new UserNotFoundException("Could not locate the requested user");
        
        } else {
        
            throw new DatabaseDownException("It seems like the database is down!");
        
        }
    }
}

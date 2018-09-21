package com.lightbend.lagom.recipes.cbpanel.api;

public class User {
    
    private final String name;
    private final Integer id;
    private final String location;
    
    public String getName() {
        return name;
    }
    
    public Integer getId() {
        return id;
    }
    
    public String getLocation() {
        return location;
    }
    
    public User(String name, Integer id, String location) {
        this.name = name;
        this.id = id;
        this.location = location;
    }
}

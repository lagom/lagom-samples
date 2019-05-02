package com.lightbend.lagom.samples.mixedpersistence.hello.impl.readside;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserGreetingRecord {
    private String id;
    private String message;

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

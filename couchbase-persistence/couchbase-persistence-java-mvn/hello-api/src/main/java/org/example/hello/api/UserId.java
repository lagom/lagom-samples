package org.example.hello.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

@Value
public class UserId {
    String value;

    public UserId(String value) {
        this.value = Preconditions.checkNotNull(value, "UserId MUST not be null");
    }

    public static UserId deserialize(String str) {
        return new UserId(str);
    }

    public static String serialize(UserId value) {
        return value.value;
    }
}

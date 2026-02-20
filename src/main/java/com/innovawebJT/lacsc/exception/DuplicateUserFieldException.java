package com.innovawebJT.lacsc.exception;

import lombok.Getter;

@Getter
public class DuplicateUserFieldException extends RuntimeException {
    private final String field;
    private final String value;

    public DuplicateUserFieldException(String field, String value) {
        super("Duplicate value for field: " + field);
        this.field = field;
        this.value = value;
    }

}

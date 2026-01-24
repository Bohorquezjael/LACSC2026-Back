package com.innovawebJT.lacsc.exception;

public class DuplicateUserFieldException extends RuntimeException {
    private final String field;
    private final String value;

    public DuplicateUserFieldException(String field, String value) {
        super("Duplicate value for field: " + field);
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}

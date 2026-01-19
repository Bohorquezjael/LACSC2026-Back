package com.innovawebJT.lacsc.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Category {
    STUDENT_POSTGRADUATE,
    STUDENT_UNDERGRADUATE,
    PROFESSIONAL;

     @JsonCreator
    public static Category from(String value) {
        return Category.valueOf(value.toUpperCase());
    }
}

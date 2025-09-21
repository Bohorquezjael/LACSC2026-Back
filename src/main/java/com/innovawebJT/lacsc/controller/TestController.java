package com.innovawebJT.lacsc.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello-1")
    @PreAuthorize("hasRole('admin-client')")
    public String helloAdmin(){
        return "Hello Spring Boot With Keycloak with ADMIN";
    }

    @GetMapping("/hello-2")
    @PreAuthorize("hasRole('user-client') or hasRole('admin-client')")
    public String helloUser(){
        return "Hello Spring Boot With Keycloak with USER";
    }
}
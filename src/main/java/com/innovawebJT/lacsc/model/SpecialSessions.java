package com.innovawebJT.lacsc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity

public class SpecialSessions {

	@Id()
	Long id;

	String name;

	String acronym;
}

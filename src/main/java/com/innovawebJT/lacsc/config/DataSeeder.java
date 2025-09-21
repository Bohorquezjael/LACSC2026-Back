package com.innovawebJT.lacsc.config;

import com.innovawebJT.lacsc.enums.Category;
import com.innovawebJT.lacsc.model.Institution;
import com.innovawebJT.lacsc.model.User;
import com.innovawebJT.lacsc.repository.UserRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Locale;

@Configuration
@Profile("dev")
public class DataSeeder {

	@Bean
	CommandLineRunner loadFakeUsers(UserRepository repository) {
		return args -> {
			Faker faker = new Faker(Locale.ENGLISH);
			for (int i = 0; i < 400; i++) {
				User user = User.builder()
						.name(faker.name().firstName())
						.surname(faker.name().lastName())
						.email(faker.internet().emailAddress())
						.badgeName(faker.name().username())
						.category(Category.STUDENT)
						.institution(
								new Institution(
										faker.university().name()
										,faker.university().prefix()
										,faker.university().place()
								))
						.build();

				repository.save(user);
			}
		};
	}
}

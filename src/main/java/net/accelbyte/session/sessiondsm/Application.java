package net.accelbyte.session.sessiondsm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		String dsProvider = System.getenv("DS_PROVIDER");
		if (dsProvider == null || dsProvider.isEmpty()) {
			dsProvider = "DEMO"; // Default value
		}
		System.setProperty("spring.profiles.active", dsProvider);

		SpringApplication.run(Application.class, args);
	}

}

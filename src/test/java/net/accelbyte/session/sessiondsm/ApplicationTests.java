package net.accelbyte.session.sessiondsm;

import net.accelbyte.session.sessiondsm.config.MockedAppConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
		classes = MockedAppConfiguration.class,
		properties = "spring.main.allow-bean-definition-overriding=true"
)
@ActiveProfiles("test")
class ApplicationTests {

	@Test
	void contextLoads() {
		
	}

}

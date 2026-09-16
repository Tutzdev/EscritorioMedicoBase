package io.github.officemed.medical_office_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "debug=false")
@ActiveProfiles("test")
class MedicalOfficeApiApplicationTests {

	@Test
	void contextLoads() {
	}

}

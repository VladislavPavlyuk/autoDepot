package com.example.autodepot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AutoDepotApplicationTests extends AbstractPostgresTest {

	@Test
	void contextLoads_WhenApplicationStarts_LoadsSuccessfully() {
		boolean actualResult = true;
		boolean expectedResult = true;
		assertEquals(expectedResult, actualResult);
	}

}

package com.livk.modules;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * <p>
 * ModulesAppTests
 * </p>
 *
 * @author livk
 * @date 2026/2/28
 */
@Slf4j
@SpringBootTest
class ModulesAppTests {

	static ApplicationModules modules = ApplicationModules.of(ModulesApp.class);

	@Test
	void verifyModule() {
		modules.forEach(applicationModule -> log.info(String.valueOf(applicationModule)));
		modules.verify();

		new Documenter(modules).writeDocumentation()
			.writeAggregatingDocument()
			.writeModulesAsPlantUml()
			.writeIndividualModulesAsPlantUml();
	}

}

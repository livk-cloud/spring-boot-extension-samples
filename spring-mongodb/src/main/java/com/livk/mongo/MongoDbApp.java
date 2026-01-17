package com.livk.mongo;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.mongodb.MongoMetricsCommandListener;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.mongodb.autoconfigure.MongoConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;

/**
 * @author livk
 */
@SpringBootApplication
public class MongoDbApp {

	void main(String[] args) {
		SpringApplication.run(MongoDbApp.class, args);
	}

	@Bean
	public MongoConverter mappingMongoConverter(MongoDatabaseFactory factory, MongoMappingContext context,
			CustomConversions customConversions) {
		var resolver = new DefaultDbRefResolver(factory);
		var converter = new MappingMongoConverter(resolver, context);
		converter.setCustomConversions(customConversions);
		converter.setTypeMapper(new DefaultMongoTypeMapper());
		return converter;
	}

	@Bean
	public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer(
			ObservationRegistry observationRegistry, MongoConnectionDetails connectionDetails,
			MeterRegistry meterRegistry) {
		return builder -> builder
			.addCommandListener(
					new MongoObservationCommandListener(observationRegistry, connectionDetails.getConnectionString()))
			.addCommandListener(new MongoMetricsCommandListener(meterRegistry));
	}

}

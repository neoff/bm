package org.neov.unicorn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

//@EnableJms
@Configuration
public class ActiveMQConfig {
	public static final String HEALTH_QUEUE = "TrafficLights.health";
	public static final String CLIENT_QUEUE = "TrafficLights.client";
	public static final String SERVER_QUEUE = "TrafficLights.server";

	@Autowired
	private void jacksonObjectMapperConfigurer(ObjectMapper objectMapper) {
		objectMapper.registerModule(new Jdk8Module());
		objectMapper.registerModule(new JavaTimeModule());

		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
		objectMapper.configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, true);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
	}


	@Bean
	public MessageConverter messageConverter(@Autowired ObjectMapper objectMapper) {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setObjectMapper(objectMapper);
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_type");
		return converter;
	}
}

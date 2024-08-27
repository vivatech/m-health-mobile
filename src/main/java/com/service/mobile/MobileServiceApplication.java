package com.service.mobile;

import com.service.mobile.storage.StorageProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class MobileServiceApplication {
	@Value("${Server.time.zone}")
	private String serverTimeZone;
	@PostConstruct
	public void init() {
		// Set default timezone to Somalia
		TimeZone.setDefault(TimeZone.getTimeZone(serverTimeZone));
	}

	public static void main(String[] args) {
		SpringApplication.run(MobileServiceApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}

}

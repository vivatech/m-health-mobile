package com.service.mobile;

import com.service.mobile.config.ServerProperties;
import com.service.mobile.storage.StorageProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, ServerProperties.class})
public class MobileServiceApplication {
	@Autowired
	private ServerProperties serverProperties;

	@Value("${server.time.zone}")
	private String serverTime;

	@PostConstruct
	public void init() {
		String serverTimeZone = serverProperties.getTimeZone() == null ? serverTime : serverProperties.getTimeZone();
		if (serverTimeZone != null) {
			TimeZone.setDefault(TimeZone.getTimeZone(serverTimeZone));
			System.out.println("Application default time zone set to: " + TimeZone.getDefault().getID());
		} else {
			System.out.println("Time zone property not set.");
		}
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

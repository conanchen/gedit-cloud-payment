package com.github.conanchen.gedit.payment;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class CloudPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudPaymentApplication.class, args);
	}

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        System.out.println("Spring Container is destroy! Customer clean up start ...");
        System.out.println("Spring Container is destroy! Customer clean up end");
    }

}

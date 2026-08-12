package dev.ridill.oar_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OarServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OarServerApplication.class, args);
	}

}

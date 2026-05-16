package com.sena.springpoo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class SpringpooApplication {

	private static final Logger log = LogManager.getLogger(SpringpooApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringpooApplication.class, args);
		log.info("✅ SENA Store iniciada correctamente");
	}

	@PreDestroy
	public void onShutdown() {
		log.info("🛑 SENA Store cerrada correctamente");
	}

}
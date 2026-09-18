package com.hamsa.fittrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FittrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(FittrackApplication.class, args);
	}

}

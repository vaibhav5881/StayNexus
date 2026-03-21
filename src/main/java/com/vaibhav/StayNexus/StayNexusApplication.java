package com.vaibhav.StayNexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * StayNexusApplication — entry point for the entire Spring Boot application.
 *
 * @SpringBootApplication is a shortcut for three annotations:
 *   @Configuration      — this class can define Spring beans
 *   @EnableAutoConfiguration — Spring auto-configures things based on
 *                              what's on the classpath (e.g., sees PostgreSQL
 *                              driver → sets up a DataSource automatically)
 *   @ComponentScan      — scans com.vaibhav.StayNexus and all sub-packages
 *                         for @Component, @Service, @Repository, @Controller etc.
 *
 * @EnableScheduling — activates the @Scheduled annotation.
 *   We use this in PricingUpdateService to run the hourly price re-calculation job.
 *   Without this annotation, @Scheduled methods silently do nothing.
 */
@SpringBootApplication
@EnableScheduling
public class StayNexusApplication {

	public static void main(String[] args) {
		SpringApplication.run(StayNexusApplication.class, args);
	}

}

package com.transactionriskengine.transactioningestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TransactionIngestionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionIngestionServiceApplication.class, args);
	}

}

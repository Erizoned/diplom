package com.college.receipt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.college.receipt.controllers.RecipeController.logger;

@SpringBootApplication(scanBasePackages = "com.college.receipt")
public class ReceiptApplication {

	public static void main(String[] args) {
		logger.info("=== Запуск приложения ReceiptApplication ===");
		SpringApplication.run(ReceiptApplication.class, args);
		logger.info("=== Приложение ReceiptApplication успешно запущено ===");
	}

}

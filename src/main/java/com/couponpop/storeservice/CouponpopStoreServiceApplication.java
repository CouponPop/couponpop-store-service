package com.couponpop.storeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CouponpopStoreServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CouponpopStoreServiceApplication.class, args);
	}

}

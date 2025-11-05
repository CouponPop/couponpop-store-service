package com.couponpop.storeservice;

import com.couponpop.storeservice.common.config.TestElasticsearchConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestElasticsearchConfig.class)
class CouponpopStoreServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}

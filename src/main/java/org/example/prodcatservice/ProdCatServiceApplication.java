package org.example.prodcatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
//@EntityScan(basePackages = "org.example.prodcatservice.models")
@EnableScheduling
@EnableDiscoveryClient
public class ProdCatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProdCatServiceApplication.class, args);
    }

}

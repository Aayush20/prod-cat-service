package org.example.prodcatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableDiscoveryClient
@EnableCaching
@EnableFeignClients(basePackages = "org.example.prodcatservice.clients")
@EnableAspectJAutoProxy
public class ProdCatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProdCatServiceApplication.class, args);
    }

}

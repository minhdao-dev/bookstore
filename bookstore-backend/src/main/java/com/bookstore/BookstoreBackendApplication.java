package com.bookstore;

import com.bookstore.auth.security.JwtProperties;
import com.bookstore.content.MinioProperties;
import com.bookstore.payment.vnpay.VNPayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, VNPayProperties.class, MinioProperties.class})
public class BookstoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreBackendApplication.class, args);
    }

}

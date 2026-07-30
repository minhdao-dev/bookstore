package com.bookstore;

import com.bookstore.auth.security.CorsProperties;
import com.bookstore.auth.security.JwtProperties;
import com.bookstore.auth.security.RateLimitProperties;
import com.bookstore.content.FfmpegProperties;
import com.bookstore.content.MinioProperties;
import com.bookstore.notification.NotificationProperties;
import com.bookstore.order.OrderExpiryProperties;
import com.bookstore.payment.vnpay.VNPayProperties;
import com.bookstore.shipping.ghn.GhnProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, VNPayProperties.class, MinioProperties.class,
        GhnProperties.class, FfmpegProperties.class, NotificationProperties.class, OrderExpiryProperties.class,
        RateLimitProperties.class, CorsProperties.class})
public class BookstoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreBackendApplication.class, args);
    }

}
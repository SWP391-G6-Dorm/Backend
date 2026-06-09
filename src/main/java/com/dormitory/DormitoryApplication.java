package com.dormitory;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DormitoryApplication {

    public static void main(String[] args) {
        try {
            String dir = java.nio.file.Files.exists(java.nio.file.Paths.get(".env")) ? "./" : "../";
            Dotenv dotenv = Dotenv.configure()
                    .directory(dir)
                    .ignoreIfMissing()
                    .load();
            
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
            System.out.println("[DEV] Đã nạp thành công các biến từ file " + dir + ".env!");
        } catch (Exception e) {
            System.out.println("[DEV] Không tìm thấy file .env.");
        }

        SpringApplication.run(DormitoryApplication.class, args);
    }
}

package com.homestay;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HomestayApplication {

    // Tất cả biến bắt buộc cần load từ .env vào System properties
    private static final String[] ENV_KEYS = {
        "JWT_SECRET",
        "MAIL_USERNAME",
        "MAIL_PASSWORD",
        "GOOGLE_CLIENT_ID",
    };

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(HomestayApplication.class, args);
    }

    /**
     * Load file .env vào System properties trước khi Spring khởi động.
     * Thử nhiều thư mục vì working directory khác nhau tuỳ cách chạy
     * (IntelliJ, Maven CLI từ backend/, command line từ root project...).
     *
     * Dùng dotenv.get(key) thay vì dotenv.entries() để tránh
     * incompatibility giữa các phiên bản của dotenv-java.
     */
    private static void loadDotenv() {
        String[] candidates = {
            ".",         // chạy từ backend/ (IntelliJ default)
            "../",       // chạy từ SWP391_G6/ root
            "./backend", // dự phòng
        };

        for (String dir : candidates) {
            try {
                Dotenv dotenv = Dotenv.configure()
                        .directory(dir)
                        .ignoreIfMissing()
                        .load();

                // Dùng dotenv.get(key) — API ổn định trên mọi phiên bản
                int count = 0;
                for (String key : ENV_KEYS) {
                    String value = dotenv.get(key, null);
                    if (value != null && !value.isBlank()) {
                        System.setProperty(key, value);
                        count++;
                    }
                }

                if (count > 0) {
                    System.out.println("[Dotenv] Loaded " + count + "/" + ENV_KEYS.length
                            + " variables from: " + dir + "/.env");
                    return; // Thành công — dừng tìm kiếm
                }

            } catch (Exception ignored) {
                // Thư mục không tồn tại hoặc không có .env → thử tiếp
            }
        }

        // Không tìm thấy .env — fallback values trong application.yml sẽ được dùng
        System.out.println("[Dotenv] .env not found — using application.yml defaults");
    }
}

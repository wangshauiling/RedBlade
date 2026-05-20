package com.redblade.init;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 数据库初始化独立启动类
 *
 * 注意：此启动类仅用于独立打包初始化工具
 * 实际使用时，建议通过 rb-admin 启动项目，初始化会自动执行
 *
 * 如需单独运行初始化，请确保所有模块的表定义类都在classpath中
 */
@SpringBootApplication(scanBasePackages = "com.redblade")
public class DbInitApplication {

    public static void main(String[] args) {
        // 检查是否包含 -db 参数
        boolean isDbInit = false;
        for (String arg : args) {
            if (arg.equals("-db") || arg.equals("--db-init")) {
                isDbInit = true;
                break;
            }
        }

        if (!isDbInit) {
            System.out.println("错误: 请使用 -db 参数启动数据库初始化");
            System.out.println("示例: java -jar redblade-init.jar -db");
            System.out.println("");
            System.out.println("或者通过 rb-admin 启动项目，初始化会自动执行");
            System.exit(1);
        }

        System.out.println("============================================================");
        System.out.println("               RedBlade 数据库初始化工具");
        System.out.println("============================================================");
        System.out.println();

        // 设置激活的profile
        System.setProperty("spring.profiles.active", "init-db");

        SpringApplication.run(DbInitApplication.class, args);
    }
}
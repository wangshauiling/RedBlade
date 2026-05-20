package com.redblade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RedBlade 主启动类
 *
 * 启动模式：
 * 1. 正常启动项目: java -jar redblade-admin.jar
 * 2. 仅初始化数据库: java -jar redblade-admin.jar -db
 */
@SpringBootApplication
public class
RedBladeApplication {

    public static void main(String[] args) {
        // 检查是否包含 -db 参数
        boolean isDbInit = false;
        for (String arg : args) {
            if (arg.equals("-db") || arg.equals("--db-init")) {
                isDbInit = true;
                break;
            }
        }

        if (isDbInit) {
            System.out.println("============================================================");
            System.out.println("               RedBlade 数据库初始化模式");
            System.out.println("============================================================");
            System.out.println();
            System.setProperty("spring.profiles.active", "init-db");
        }

        SpringApplication.run(RedBladeApplication.class, args);

        // 如果是初始化模式，完成后退出
        if (isDbInit) {
            System.out.println();
            System.out.println("============================================================");
            System.out.println("         数据库初始化完成，程序退出");
            System.out.println("============================================================");
            System.exit(0);
        }
    }
}
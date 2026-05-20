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
public class RedBladeApplication {

    public static void main(String[] args) {
        // 检查是否包含 -db 参数
        for (String arg : args) {
            if (arg.equals("-db") || arg.equals("--db-init")) {
                System.out.println("============================================================");
                System.out.println("               RedBlade 数据库初始化模式");
                System.out.println("============================================================");
                System.out.println();
                System.out.println("提示: 请使用 redblade-init.jar 进行数据库初始化");
                System.out.println("示例: java -jar redblade-init.jar -db");
                System.out.println();
                System.exit(0);
                return;
            }
        }

        // 正常启动项目
        SpringApplication.run(RedBladeApplication.class, args);
    }
}
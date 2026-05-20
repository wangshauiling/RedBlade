package com.redblade.init.api;

import java.util.Scanner;
import java.util.function.Function;

/**
 * 用户输入工具
 * 用于交互式初始化时获取用户输入
 */
public class UserInput {

    private final Scanner scanner;

    public UserInput() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * 获取用户输入（带提示）
     *
     * @param prompt 提示信息
     * @return 用户输入内容
     */
    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * 获取用户输入（带默认值）
     *
     * @param prompt      提示信息
     * @param defaultValue 默认值
     * @return 用户输入内容（空则返回默认值）
     */
    public String readLine(String prompt, String defaultValue) {
        String input = readLine(prompt);
        return input.isEmpty() ? defaultValue : input;
    }

    /**
     * 获取用户输入（带校验）
     *
     * @param prompt   提示信息
     * @param validator 校验函数，返回 null 表示校验通过，返回错误信息表示校验失败
     * @return 用户输入内容
     */
    public String readLineWithValidation(String prompt, Function<String, String> validator) {
        while (true) {
            String input = readLine(prompt);
            String error = validator.apply(input);
            if (error == null) {
                return input;
            }
            System.out.println("  " + error);
        }
    }

    /**
     * 获取用户选择（数字选项）
     *
     * @param prompt   提示信息
     * @param options  选项列表
     * @param defaultIndex 默认选项索引（从1开始）
     * @return 用户选择的选项值
     */
    public String readChoice(String prompt, String[] options, int defaultIndex) {
        System.out.println(prompt);
        for (int i = 0; i < options.length; i++) {
            System.out.println("    " + (i + 1) + ". " + options[i]);
        }
        String input = readLine("  请选择 [默认: " + defaultIndex + "]: ");
        if (input.isEmpty()) {
            return options[defaultIndex - 1];
        }
        try {
            int index = Integer.parseInt(input);
            if (index >= 1 && index <= options.length) {
                return options[index - 1];
            }
        } catch (NumberFormatException ignored) {
        }
        return options[defaultIndex - 1];
    }

    /**
     * 获取确认输入
     *
     * @param prompt 提示信息
     * @return true 表示确认，false 表示取消
     */
    public boolean readConfirm(String prompt) {
        while (true) {
            String input = readLine(prompt + " [Y/n]: ");
            if (input.isEmpty() || input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {
                return true;
            }
            if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) {
                return false;
            }
            System.out.println("  请输入 Y 或 N");
        }
    }

    /**
     * 获取密码输入（隐藏显示）
     * 注意：控制台可能不支持隐藏，此方法仅做简单处理
     *
     * @param prompt 提示信息
     * @return 用户输入的密码
     */
    public String readPassword(String prompt) {
        System.out.print(prompt);
        // 简单实现，实际可能需要使用 Console.readPassword()
        return scanner.nextLine().trim();
    }

    /**
     * 打印分隔线
     */
    public void printSeparator() {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
    }

    /**
     * 打印分隔线结束
     */
    public void printSeparatorEnd() {
        System.out.println("└────────────────────────────────────────────────────────────┘");
    }

    /**
     * 打印标题
     */
    public void printTitle(String title) {
        printSeparator();
        int padding = (56 - title.length()) / 2;
        System.out.println("│" + " ".repeat(padding) + title + " ".repeat(56 - padding - title.length()) + "│");
        printSeparatorEnd();
        System.out.println();
    }

    /**
     * 关闭 Scanner
     */
    public void close() {
        scanner.close();
    }
}
package com.redblade.init.api;

import com.redblade.init.metadata.TableDefinition;

import java.util.List;
import java.util.Map;

/**
 * 数据库初始化扩展点接口
 * 二次开发项目可实现此接口，自定义初始化逻辑
 *
 * 使用示例：
 * <pre>
 * &#64;Component
 * public class MyProjectInitExtension implements DbInitExtension {
 *     &#64;Override
 *     public List&lt;TableDefinition&gt; getTableDefinitions() {
 *         return List.of(
 *             new BizOrderTable(),
 *             new BizProductTable()
 *         );
 *     }
 *
 *     &#64;Override
 *     public void afterInit(InitContext context) {
 *         // 初始化完成后执行自定义逻辑
 *         loadDefaultData();
 *     }
 * }
 * </pre>
 */
public interface DbInitExtension {

    /**
     * 获取扩展名称
     * 用于日志输出
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * 获取扩展顺序
     * 数字越小越先执行
     */
    default int getOrder() {
        return 100;
    }

    /**
     * 提供额外的表定义
     * 二次开发项目可添加自己的业务表
     */
    default List<TableDefinition> getTableDefinitions() {
        return List.of();
    }

    /**
     * 初始化前回调
     * 可用于检查前置条件、打印提示信息
     */
    default void beforeInit(InitContext context) {
        // 默认空实现
    }

    /**
     * 表结构创建完成后回调
     * 可用于创建视图、存储过程等
     */
    default void afterTablesCreated(InitContext context) {
        // 默认空实现
    }

    /**
     * 交互式输入回调
     * 首次运行时，可在控制台引导用户输入自定义配置
     *
     * @param context 初始化上下文
     * @param input   用户输入工具
     */
    default void onInteractiveInput(InitContext context, UserInput input) {
        // 默认空实现
    }

    /**
     * 初始化完成后回调
     * 可用于加载默认数据、发送通知等
     */
    default void afterInit(InitContext context) {
        // 默认空实现
    }

    /**
     * 提供初始数据
     * 返回的数据会在初始化时自动插入
     */
    default List<InitData> getInitData() {
        return List.of();
    }

    /**
     * 是否启用此扩展
     * 可根据配置或环境决定是否执行
     */
    default boolean isEnabled() {
        return true;
    }
}
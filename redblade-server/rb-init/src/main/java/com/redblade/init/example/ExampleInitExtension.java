package com.redblade.init.example;

import com.redblade.init.api.*;
import com.redblade.init.metadata.ColumnDefinition;
import com.redblade.init.metadata.DataType;
import com.redblade.init.metadata.TableDefinition;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 二次开发项目扩展示例
 *
 * 展示如何在二次开发项目中扩展数据库初始化逻辑：
 * 1. 添加自定义业务表
 * 2. 添加自定义初始数据
 * 3. 在初始化完成后执行自定义逻辑
 */
@Component
public class ExampleInitExtension implements DbInitExtension {

    @Override
    public String getName() {
        return "业务模块初始化";
    }

    @Override
    public int getOrder() {
        return 200; // 在框架核心之后执行
    }

    /**
     * 提供业务表定义
     */
    @Override
    public List<TableDefinition> getTableDefinitions() {
        return List.of(
            // 订单表
            new TableDefinition() {
                @Override
                public String getTableName() {
                    return "biz_order";
                }

                @Override
                public String getComment() {
                    return "订单表";
                }

                @Override
                public List<ColumnDefinition> getColumns() {
                    return List.of(
                        ColumnDefinition.builder().name("org_code").type(DataType.VARCHAR).length(50).primaryKey(true).comment("组织编码").build(),
                        ColumnDefinition.builder().name("order_id").type(DataType.BIGINT).primaryKey(true).autoIncrement(true).comment("订单ID").build(),
                        ColumnDefinition.builder().name("order_no").type(DataType.VARCHAR).length(50).comment("订单号").build(),
                        ColumnDefinition.builder().name("customer_name").type(DataType.VARCHAR).length(100).comment("客户名称").build(),
                        ColumnDefinition.builder().name("total_amount").type(DataType.DECIMAL).length(12).scale(2).comment("总金额").build(),
                        ColumnDefinition.builder().name("status").type(DataType.CHAR).length(1).defaultValue("'0'").comment("状态").build(),
                        ColumnDefinition.builder().name("create_time").type(DataType.DATETIME).comment("创建时间").build(),
                        ColumnDefinition.builder().name("del_flag").type(DataType.CHAR).length(1).defaultValue("'0'").comment("删除标志").build()
                    );
                }
            }
        );
    }

    /**
     * 表创建完成后回调
     * 可用于创建视图、索引等
     */
    @Override
    public void afterTablesCreated(InitContext context) {
        // 示例：创建自定义索引
        System.out.println("  [扩展] 创建业务模块索引...");
    }

    /**
     * 交互式输入回调
     * 可添加自定义配置项
     */
    @Override
    public void onInteractiveInput(InitContext context, UserInput input) {
        input.printTitle("业务模块配置");

        // 示例：询问是否初始化演示数据
        boolean initDemo = input.readConfirm("  是否初始化演示数据?");
        context.setAttribute("initDemoData", initDemo);
    }

    /**
     * 提供初始数据
     */
    @Override
    public List<InitData> getInitData() {
        return List.of(
            // 默认订单状态配置
            InitData.of("sys_dict_data", List.of(
                Map.of(
                    "dict_type", "order_status",
                    "dict_label", "待支付",
                    "dict_value", "0",
                    "sort", 1
                ),
                Map.of(
                    "dict_type", "order_status",
                    "dict_label", "已支付",
                    "dict_value", "1",
                    "sort", 2
                )
            )).order(100)
        );
    }

    /**
     * 初始化完成后回调
     * 可用于发送通知、记录日志等
     */
    @Override
    public void afterInit(InitContext context) {
        System.out.println("  [扩展] 业务模块初始化完成");

        // 如果用户选择初始化演示数据
        if (context.getAttribute("initDemoData", false)) {
            System.out.println("  [扩展] 加载演示数据...");
            // 加载演示数据逻辑
        }
    }
}
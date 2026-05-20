package com.redblade.init.metadata;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 索引定义
 */
@Data
@Builder
public class IndexDefinition {

    /**
     * 索引名
     */
    private String name;

    /**
     * 索引字段
     */
    private List<String> columns;

    /**
     * 是否唯一索引
     */
    private boolean unique;

    /**
     * 索引类型
     */
    private String type;
}
package com.redblade.model.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 查询参数
 */
@Data
public class QueryParams implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式（asc/desc）
     */
    private String sortOrder = "desc";

    /**
     * 查询条件
     */
    private Map<String, Object> conditions;

    /**
     * 搜索关键词（模糊查询）
     */
    private String keyword;

    /**
     * 搜索字段列表
     */
    private List<String> searchFields;

    /**
     * 时间范围查询 - 开始时间
     */
    private String startTime;

    /**
     * 时间范围查询 - 结束时间
     */
    private String endTime;

    /**
     * 时间范围字段
     */
    private String timeField;

    /**
     * 组织编码（自动注入）
     */
    private String orgCode;

    /**
     * 是否分页
     */
    private Boolean pagination = true;

    /**
     * 导出类型（excel/csv）
     */
    private String exportType;
}
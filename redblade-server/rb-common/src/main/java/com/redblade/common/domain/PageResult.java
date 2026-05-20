package com.redblade.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总数
     */
    private long total;

    /**
     * 数据列表
     */
    private List<T> rows;

    public PageResult() {
    }

    public PageResult(long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public static <T> PageResult<T> of(long total, List<T> rows) {
        return new PageResult<>(total, rows);
    }
}
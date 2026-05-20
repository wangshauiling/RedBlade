package com.redblade.init.metadata.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 字典数据传输对象
 * 用于初始化字典数据
 */
@Data
public class DictDto {

    /**
     * 字典编码（主键）
     */
    private String dictCode;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典值
     */
    private String dictValue;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    public DictDto() {
    }

    public DictDto(String dictType, String dictLabel, String dictValue) {
        this.dictCode = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        this.dictType = dictType;
        this.dictLabel = dictLabel;
        this.dictValue = dictValue;
        this.status = "0";
    }

    public DictDto(String dictType, String dictLabel, String dictValue, Integer sort) {
        this(dictType, dictLabel, dictValue);
        this.sort = sort;
    }

    /**
     * 设置字典编码
     */
    public DictDto dictCode(String dictCode) {
        this.dictCode = dictCode;
        return this;
    }

    /**
     * 设置排序
     */
    public DictDto sort(Integer sort) {
        this.sort = sort;
        return this;
    }

    /**
     * 设置备注
     */
    public DictDto remark(String remark) {
        this.remark = remark;
        return this;
    }

    /**
     * 转换为数据库记录
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("dict_code", dictCode);
        map.put("dict_type", dictType);
        map.put("dict_label", dictLabel);
        map.put("dict_value", dictValue);
        map.put("dict_sort", sort);  // 字段名是 dict_sort
        map.put("is_default", "N");
        map.put("status", status);
        if (remark != null) {
            map.put("remark", remark);
        }
        return map;
    }
}
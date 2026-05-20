package com.redblade.init.metadata.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 角色数据传输对象
 * 用于初始化角色数据
 */
@Data
public class RoleDto {

    /**
     * 组织编码
     */
    private String orgCode;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色标识
     */
    private String roleKey;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 数据权限范围
     */
    private String dataScope;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    public RoleDto() {
        this.orgCode = "001";
    }

    public RoleDto(String roleCode, String roleName, String roleKey) {
        this();
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.roleKey = roleKey;
        this.status = "0";
        this.dataScope = "1"; // 默认本组织数据
    }

    public RoleDto(String roleCode, String roleName, String roleKey, Integer sort) {
        this(roleCode, roleName, roleKey);
        this.sort = sort;
    }

    /**
     * 设置组织编码
     */
    public RoleDto orgCode(String orgCode) {
        this.orgCode = orgCode;
        return this;
    }

    /**
     * 设置排序
     */
    public RoleDto sort(Integer sort) {
        this.sort = sort;
        return this;
    }

    /**
     * 设置数据权限范围
     */
    public RoleDto dataScope(String dataScope) {
        this.dataScope = dataScope;
        return this;
    }

    /**
     * 设置备注
     */
    public RoleDto remark(String remark) {
        this.remark = remark;
        return this;
    }

    /**
     * 转换为数据库记录
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("org_code", orgCode);
        map.put("role_code", roleCode);
        map.put("role_name", roleName);
        map.put("role_key", roleKey);
        map.put("sort", sort);
        map.put("data_scope", dataScope);
        map.put("status", status);
        map.put("del_flag", "0");
        if (remark != null) {
            map.put("remark", remark);
        }
        return map;
    }
}

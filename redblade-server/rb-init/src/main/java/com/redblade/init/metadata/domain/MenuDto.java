package com.redblade.init.metadata.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单数据传输对象
 * 用于初始化菜单数据
 */
@Data
public class MenuDto {

    /**
     * 组织编码
     */
    private String orgCode;

    /**
     * 菜单编码（唯一标识）
     */
    private String menuCode;

    /**
     * 父菜单编码
     */
    private String parentCode;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    private String menuType;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否可见
     */
    private Boolean visible;

    /**
     * 是否缓存
     */
    private Boolean isCache;

    /**
     * 是否外链
     */
    private Boolean isFrame;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 关联的 Model 类（用于自动生成权限按钮）
     */
    private Class<?> modelClass;

    /**
     * 子菜单
     */
    private List<MenuDto> children = new ArrayList<>();

    public MenuDto() {
        this.orgCode = "001";
        this.status = "0";
        this.visible = true;
        this.isCache = false;
        this.isFrame = false;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 设置组织编码
     */
    public MenuDto orgCode(String orgCode) {
        this.orgCode = orgCode;
        return this;
    }

    /**
     * 创建目录
     */
    public static MenuDto directory(String menuCode, Integer sort, String menuName, String path) {
        MenuDto dto = new MenuDto();
        dto.setMenuCode(menuCode);
        dto.setSort(sort);
        dto.setMenuName(menuName);
        dto.setPath(path);
        dto.setMenuType("M");
        return dto;
    }

    /**
     * 创建菜单
     */
    public static MenuDto menu(String menuCode, Integer sort, String menuName, String path, String component) {
        MenuDto dto = new MenuDto();
        dto.setMenuCode(menuCode);
        dto.setSort(sort);
        dto.setMenuName(menuName);
        dto.setPath(path);
        dto.setComponent(component);
        dto.setMenuType("C");
        return dto;
    }

    /**
     * 创建按钮
     */
    public static MenuDto button(String menuCode, Integer sort, String menuName, String permission) {
        MenuDto dto = new MenuDto();
        dto.setMenuCode(menuCode);
        dto.setSort(sort);
        dto.setMenuName(menuName);
        dto.setPermission(permission);
        dto.setMenuType("F");
        dto.setVisible(false);
        return dto;
    }

    // ==================== 链式设置方法 ====================

    /**
     * 设置父菜单编码
     */
    public MenuDto parent(String parentCode) {
        this.parentCode = parentCode;
        return this;
    }

    /**
     * 设置图标
     */
    public MenuDto icon(String icon) {
        this.icon = icon;
        return this;
    }

    /**
     * 设置权限标识
     */
    public MenuDto permission(String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * 设置是否可见
     */
    public MenuDto visible(boolean visible) {
        this.visible = visible;
        return this;
    }

    /**
     * 设置是否缓存
     */
    public MenuDto cache(boolean isCache) {
        this.isCache = isCache;
        return this;
    }

    /**
     * 关联 Model 类，自动生成 CRUD 权限按钮
     *
     * @param modelClass Model 类
     * @return this
     */
    public MenuDto addAuthority(Class<?> modelClass) {
        this.modelClass = modelClass;
        return this;
    }

    /**
     * 添加子菜单
     */
    public MenuDto addChild(MenuDto child) {
        child.setParentCode(this.menuCode);
        this.children.add(child);
        return this;
    }

    /**
     * 批量添加子菜单
     */
    public MenuDto addChildren(List<MenuDto> children) {
        for (MenuDto child : children) {
            child.setParentCode(this.menuCode);
            this.children.add(child);
        }
        return this;
    }

    // ==================== 转换方法 ====================

    /**
     * 转换为数据库记录
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("menu_code", menuCode);
        map.put("parent_code", parentCode);
        map.put("menu_name", menuName);
        map.put("menu_type", menuType);
        map.put("path", path);
        map.put("component", component);
        map.put("permission", permission);
        map.put("icon", icon);
        map.put("sort", sort);
        map.put("visible", visible ? "0" : "1");
        map.put("is_cache", isCache ? "1" : "0");
        map.put("is_frame", isFrame ? "1" : "0");
        map.put("status", status);
        return map;
    }

    /**
     * 获取所有菜单（包括子菜单）扁平化列表
     */
    public List<MenuDto> flatten() {
        List<MenuDto> result = new ArrayList<>();
        result.add(this);
        for (MenuDto child : children) {
            result.addAll(child.flatten());
        }
        return result;
    }
}

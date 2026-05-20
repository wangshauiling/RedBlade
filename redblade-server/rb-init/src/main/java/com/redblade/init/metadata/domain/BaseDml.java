package com.redblade.init.metadata.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * DML 基类
 * 所有初始化数据类继承此类
 *
 * @param <T> 数据类型
 */
public abstract class BaseDml<T> {

    /**
     * 数据列表
     */
    protected final List<T> dataList = new ArrayList<>();

    /**
     * 添加数据
     */
    protected void add(T data) {
        dataList.add(data);
    }

    /**
     * 批量添加数据
     */
    protected void addAll(List<T> data) {
        dataList.addAll(data);
    }

    /**
     * 添加菜单（支持子菜单扁平化）
     * 如果是 MenuDto 类型，会自动展开子菜单
     */
    protected void addMenu(MenuDto menu) {
        // 扁平化处理，将子菜单也添加进去
        List<MenuDto> flatList = menu.flatten();
        for (MenuDto m : flatList) {
            @SuppressWarnings("unchecked")
            T item = (T) m;
            dataList.add(item);
        }
    }

    /**
     * 获取所有数据
     */
    public List<T> getDataList() {
        return dataList;
    }

    /**
     * 转换为 Map 列表（用于数据库插入）
     *
     * @param converter 转换函数
     * @return Map 列表
     */
    public List<Map<String, Object>> toMapList(Function<T, Map<String, Object>> converter) {
        return dataList.stream()
            .map(converter)
            .collect(Collectors.toList());
    }

    /**
     * 转换为 Map 列表（要求数据类型实现 toMap 方法）
     *
     * @return Map 列表
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> toMapList() {
        return dataList.stream()
            .map(data -> {
                // 如果本身就是 Map，直接返回
                if (data instanceof Map) {
                    return (Map<String, Object>) data;
                }
                try {
                    // 尝试调用 toMap 方法
                    return (Map<String, Object>) data.getClass().getMethod("toMap").invoke(data);
                } catch (Exception e) {
                    throw new RuntimeException("数据转换失败，请实现 toMap 方法或使用 toMapList(Function) 方法", e);
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取数据数量
     */
    public int size() {
        return dataList.size();
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return dataList.isEmpty();
    }
}

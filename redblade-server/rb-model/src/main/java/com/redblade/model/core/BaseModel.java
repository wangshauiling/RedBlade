package com.redblade.model.core;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.redblade.common.domain.PageResult;
import com.redblade.common.domain.R;
import com.redblade.common.enums.ResultCode;
import com.redblade.common.exception.BusinessException;
import com.redblade.model.annotation.Model;
import com.redblade.model.domain.QueryParams;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Model 基类
 * 所有业务 Model 继承此类，自动获得 CRUD 能力
 * 提供 CRUD 各阶段的前后拦截钩子，供二次开发覆写
 *
 * @param <T> 实体类型
 */
@Slf4j
public abstract class BaseModel<T> extends ServiceImpl<BaseMapper<T>, T> {

    /**
     * 实体类类型
     */
    protected Class<T> entityClass;

    /**
     * Model 注解配置
     */
    protected Model modelConfig;

    @SuppressWarnings("unchecked")
    public BaseModel() {
        // 获取泛型类型
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            this.entityClass = (Class<T>) typeArguments[0];
        }

        // 获取 Model 注解
        this.modelConfig = getClass().getAnnotation(Model.class);
    }

    // ==================== 查询方法 ====================

    /**
     * 分页查询
     */
    public PageResult<T> list(QueryParams params) {
        beforeList(params);
        QueryWrapper<T> wrapper = buildQueryWrapper(params);
        processSort(wrapper, params);
        Page<T> page = new Page<>(params.getPageNum(), params.getPageSize());
        Page<T> result = page(page, wrapper);
        afterList(params, result.getRecords());
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    /**
     * 查询全部（不分页）
     */
    public List<T> listAll(QueryParams params) {
        beforeListAll(params);
        QueryWrapper<T> wrapper = buildQueryWrapper(params);
        processSort(wrapper, params);
        List<T> result = list(wrapper);
        afterListAll(params, result);
        return result;
    }

    /**
     * 根据 ID 查询
     */
    public T getOneById(Serializable id) {
        beforeGetById(id);
        T entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        afterGetById(id, entity);
        return entity;
    }

    /**
     * 根据条件查询单条
     */
    public T getOneByCondition(Map<String, Object> conditions) {
        beforeGetByCondition(conditions);
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        conditions.forEach((key, value) -> {
            if (value != null) {
                wrapper.eq(camelToSnake(key), value);
            }
        });
        T entity = getOne(wrapper);
        afterGetByCondition(conditions, entity);
        return entity;
    }

    /**
     * 根据条件查询列表
     */
    public List<T> listByCondition(Map<String, Object> conditions) {
        beforeListByCondition(conditions);
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        conditions.forEach((key, value) -> {
            if (value != null) {
                wrapper.eq(camelToSnake(key), value);
            }
        });
        List<T> result = list(wrapper);
        afterListByCondition(conditions, result);
        return result;
    }

    // ==================== 新增方法 ====================

    /**
     * 新增数据
     */
    public Serializable insert(T entity) {
        beforeInsert(entity);
        validateInsert(entity);
        boolean success = save(entity);
        if (!success) {
            throw new BusinessException("新增失败");
        }
        afterInsert(entity);
        return getEntityId(entity);
    }

    /**
     * 批量新增
     */
    public void insertBatch(List<T> entities) {
        beforeInsertBatch(entities);
        for (T entity : entities) {
            validateInsert(entity);
            beforeInsert(entity);
        }
        boolean success = saveBatch(entities);
        if (!success) {
            throw new BusinessException("批量新增失败");
        }
        for (T entity : entities) {
            afterInsert(entity);
        }
        afterInsertBatch(entities);
    }

    // ==================== 修改方法 ====================

    /**
     * 修改数据
     */
    public void update(Serializable id, T entity) {
        beforeUpdate(id, entity);
        setEntityId(entity, id);
        validateUpdate(entity);
        T existing = getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        boolean success = updateById(entity);
        if (!success) {
            throw new BusinessException("修改失败");
        }
        afterUpdate(id, entity, existing);
    }

    /**
     * 批量修改
     */
    public void updateBatch(List<T> entities) {
        beforeUpdateBatch(entities);
        for (T entity : entities) {
            Serializable id = getEntityId(entity);
            validateUpdate(entity);
            beforeUpdate(id, entity);
        }
        boolean success = updateBatchById(entities);
        if (!success) {
            throw new BusinessException("批量修改失败");
        }
        for (T entity : entities) {
            Serializable id = getEntityId(entity);
            afterUpdate(id, entity, null);
        }
        afterUpdateBatch(entities);
    }

    /**
     * 选择性修改（只修改非空字段）
     */
    public void updateSelective(Serializable id, T entity) {
        beforeUpdateSelective(id, entity);
        setEntityId(entity, id);
        T existing = getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        mergeNonNullFields(existing, entity);
        validateUpdate(existing);
        boolean success = updateById(existing);
        if (!success) {
            throw new BusinessException("修改失败");
        }
        afterUpdateSelective(id, existing, entity);
    }

    // ==================== 删除方法 ====================

    /**
     * 删除数据
     */
    public void delete(Serializable id) {
        beforeDelete(id);
        T existing = getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        validateDelete(id, existing);
        boolean success = removeById(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        afterDelete(id, existing);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<? extends Serializable> ids) {
        beforeDeleteBatch(ids);
        List<T> existingList = listByIds(ids);
        for (Serializable id : ids) {
            T existing = existingList.stream()
                .filter(e -> id.equals(getEntityId(e)))
                .findFirst()
                .orElse(null);
            if (existing == null) {
                throw new BusinessException("数据不存在: " + id);
            }
            validateDelete(id, existing);
            beforeDelete(id);
        }
        boolean success = removeByIds(ids);
        if (!success) {
            throw new BusinessException("批量删除失败");
        }
        for (T existing : existingList) {
            Serializable id = getEntityId(existing);
            afterDelete(id, existing);
        }
        afterDeleteBatch(ids, existingList);
    }

    /**
     * 根据条件删除
     */
    public void deleteByCondition(Map<String, Object> conditions) {
        beforeDeleteByCondition(conditions);
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        conditions.forEach((key, value) -> {
            if (value != null) {
                wrapper.eq(camelToSnake(key), value);
            }
        });
        List<T> existingList = list(wrapper);
        for (T existing : existingList) {
            Serializable id = getEntityId(existing);
            validateDelete(id, existing);
            beforeDelete(id);
        }
        boolean success = remove(wrapper);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        for (T existing : existingList) {
            Serializable id = getEntityId(existing);
            afterDelete(id, existing);
        }
        afterDeleteByCondition(conditions, existingList);
    }

    // ==================== 查询钩子方法 ====================

    protected void beforeList(QueryParams params) {}
    protected void afterList(QueryParams params, List<T> result) {}
    protected void beforeListAll(QueryParams params) {}
    protected void afterListAll(QueryParams params, List<T> result) {}
    protected void beforeGetById(Serializable id) {}
    protected void afterGetById(Serializable id, T entity) {}
    protected void beforeGetByCondition(Map<String, Object> conditions) {}
    protected void afterGetByCondition(Map<String, Object> conditions, T entity) {}
    protected void beforeListByCondition(Map<String, Object> conditions) {}
    protected void afterListByCondition(Map<String, Object> conditions, List<T> result) {}

    // ==================== 新增钩子方法 ====================

    protected void beforeInsert(T entity) {}
    protected void afterInsert(T entity) {}
    protected void beforeInsertBatch(List<T> entities) {}
    protected void afterInsertBatch(List<T> entities) {}
    protected void validateInsert(T entity) {}

    // ==================== 修改钩子方法 ====================

    protected void beforeUpdate(Serializable id, T entity) {}
    protected void afterUpdate(Serializable id, T entity, T existing) {}
    protected void beforeUpdateBatch(List<T> entities) {}
    protected void afterUpdateBatch(List<T> entities) {}
    protected void beforeUpdateSelective(Serializable id, T entity) {}
    protected void afterUpdateSelective(Serializable id, T existing, T entity) {}
    protected void validateUpdate(T entity) {}

    // ==================== 删除钩子方法 ====================

    protected void beforeDelete(Serializable id) {}
    protected void afterDelete(Serializable id, T existing) {}
    protected void beforeDeleteBatch(List<? extends Serializable> ids) {}
    protected void afterDeleteBatch(List<? extends Serializable> ids, List<T> existingList) {}
    protected void beforeDeleteByCondition(Map<String, Object> conditions) {}
    protected void afterDeleteByCondition(Map<String, Object> conditions, List<T> existingList) {}
    protected void validateDelete(Serializable id, T existing) {}

    // ==================== 工具方法 ====================

    protected QueryWrapper<T> buildQueryWrapper(QueryParams params) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        if (params.getConditions() != null) {
            params.getConditions().forEach((key, value) -> {
                if (value != null) {
                    wrapper.eq(camelToSnake(key), value);
                }
            });
        }
        if (params.getKeyword() != null && params.getSearchFields() != null) {
            wrapper.and(w -> {
                for (int i = 0; i < params.getSearchFields().size(); i++) {
                    String field = params.getSearchFields().get(i);
                    if (i == 0) {
                        w.like(camelToSnake(field), params.getKeyword());
                    } else {
                        w.or().like(camelToSnake(field), params.getKeyword());
                    }
                }
            });
        }
        if (params.getStartTime() != null && params.getTimeField() != null) {
            wrapper.ge(camelToSnake(params.getTimeField()), params.getStartTime());
        }
        if (params.getEndTime() != null && params.getTimeField() != null) {
            wrapper.le(camelToSnake(params.getTimeField()), params.getEndTime());
        }
        return wrapper;
    }

    protected void processSort(QueryWrapper<T> wrapper, QueryParams params) {
        if (params.getSortField() != null) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(params.getSortOrder()),
                camelToSnake(params.getSortField()));
        } else if (modelConfig != null) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(modelConfig.sortOrder()),
                camelToSnake(modelConfig.sortField()));
        }
    }

    protected void mergeNonNullFields(T target, T source) {
        try {
            for (java.lang.reflect.Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(source);
                if (value != null) {
                    field.set(target, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("合并字段失败", e);
        }
    }

    protected String camelToSnake(String str) {
        if (str == null) return null;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 获取实体 ID
     */
    @SuppressWarnings("unchecked")
    protected Serializable getEntityId(T entity) {
        try {
            // 尝试获取主键字段
            String entityName = entityClass.getSimpleName();
            String idFieldName = entityName.replace("Sys", "").replace("Entity", "").toLowerCase() + "Code";

            // 尝试 getXxxCode 方法
            try {
                String methodName = "get" + Character.toUpperCase(idFieldName.charAt(0)) + idFieldName.substring(1);
                return (Serializable) entity.getClass().getMethod(methodName).invoke(entity);
            } catch (NoSuchMethodException e) {
                // 尝试 getId 方法
                return (Serializable) entity.getClass().getMethod("getId").invoke(entity);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置实体 ID
     */
    protected void setEntityId(T entity, Serializable id) {
        try {
            String entityName = entityClass.getSimpleName();
            String idFieldName = entityName.replace("Sys", "").replace("Entity", "").toLowerCase() + "Code";

            // 尝试 setXxxCode 方法
            try {
                String methodName = "set" + Character.toUpperCase(idFieldName.charAt(0)) + idFieldName.substring(1);
                java.lang.reflect.Method method = entity.getClass().getMethod(methodName, id.getClass());
                method.invoke(entity, id);
            } catch (NoSuchMethodException e) {
                // 尝试 setId 方法
                java.lang.reflect.Method method = entity.getClass().getMethod("setId", id.getClass());
                method.invoke(entity, id);
            }
        } catch (Exception e) {
            throw new RuntimeException("设置ID失败", e);
        }
    }

    public String getModelName() {
        return modelConfig != null ? modelConfig.name() : getClass().getSimpleName();
    }

    public String getTableName() {
        return modelConfig != null ? modelConfig.table() : "";
    }

    public String getApiPath() {
        return modelConfig != null ? modelConfig.api() : "";
    }

    protected boolean existsById(Serializable id) {
        return getById(id) != null;
    }

    protected boolean existsByField(String fieldName, Object value) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.eq(camelToSnake(fieldName), value);
        return count(wrapper) > 0;
    }
}
package com.redblade.model.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redblade.common.domain.PageResult;
import com.redblade.common.domain.R;
import com.redblade.common.exception.BusinessException;
import com.redblade.model.annotation.Model;
import com.redblade.model.annotation.ModelMethod;
import com.redblade.model.domain.QueryParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Model 统一控制器
 * 自动注册所有 Model 的 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
public class ModelController {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    private final Map<String, ModelInfo> modelRegistry = new HashMap<>();

    @PostConstruct
    public void init() {
        Map<String, BaseModel> models = applicationContext.getBeansOfType(BaseModel.class);
        for (Map.Entry<String, BaseModel> entry : models.entrySet()) {
            BaseModel<?> model = entry.getValue();
            Model annotation = model.getClass().getAnnotation(Model.class);
            if (annotation != null) {
                String modelKey = getModelKey(model.getClass());
                ModelInfo info = new ModelInfo(model, annotation);
                modelRegistry.put(modelKey, info);
                log.info("注册 Model: {} -> {} [table={}, name={}]",
                    modelKey, model.getClass().getSimpleName(), annotation.table(), annotation.name());
            }
        }
        log.info("Model 注册完成，共 {} 个", modelRegistry.size());
    }

    @GetMapping("/{model}/list")
    public R<PageResult<?>> list(@PathVariable String model, QueryParams params) {
        ModelInfo info = getModelInfo(model);
        return R.ok(info.getModel().list(params));
    }

    @GetMapping("/{model}/all")
    public R<List<?>> listAll(@PathVariable String model, QueryParams params) {
        ModelInfo info = getModelInfo(model);
        return R.ok(info.getModel().listAll(params));
    }

    @GetMapping("/{model}/{id}")
    public R<?> get(@PathVariable String model, @PathVariable String id) {
        ModelInfo info = getModelInfo(model);
        return R.ok(info.getModel().getOneById(id));
    }

    @GetMapping("/{model}/query")
    public R<List<?>> query(@PathVariable String model, @RequestParam Map<String, Object> conditions) {
        ModelInfo info = getModelInfo(model);
        return R.ok(info.getModel().listByCondition(conditions));
    }

    @PostMapping("/{model}")
    @SuppressWarnings("unchecked")
    public R<String> insert(@PathVariable String model, @RequestBody Map<String, Object> data) {
        ModelInfo info = getModelInfo(model);
        Object entity = convertToEntity(info, data);
        BaseModel<Object> modelInstance = (BaseModel<Object>) info.getModel();
        Serializable id = modelInstance.insert(entity);
        return R.ok(id != null ? id.toString() : null);
    }

    @PostMapping("/{model}/batch")
    @SuppressWarnings("unchecked")
    public R<Void> insertBatch(@PathVariable String model, @RequestBody List<Map<String, Object>> dataList) {
        ModelInfo info = getModelInfo(model);
        List<Object> entities = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            entities.add(convertToEntity(info, data));
        }
        BaseModel<Object> modelInstance = (BaseModel<Object>) info.getModel();
        modelInstance.insertBatch(entities);
        return R.ok();
    }

    @PutMapping("/{model}/{id}")
    @SuppressWarnings("unchecked")
    public R<Void> update(@PathVariable String model, @PathVariable String id,
                          @RequestBody Map<String, Object> data) {
        ModelInfo info = getModelInfo(model);
        Object entity = convertToEntity(info, data);
        BaseModel<Object> modelInstance = (BaseModel<Object>) info.getModel();
        modelInstance.update(id, entity);
        return R.ok();
    }

    @PatchMapping("/{model}/{id}")
    @SuppressWarnings("unchecked")
    public R<Void> updateSelective(@PathVariable String model, @PathVariable String id,
                                   @RequestBody Map<String, Object> data) {
        ModelInfo info = getModelInfo(model);
        Object entity = convertToEntity(info, data);
        BaseModel<Object> modelInstance = (BaseModel<Object>) info.getModel();
        modelInstance.updateSelective(id, entity);
        return R.ok();
    }

    @DeleteMapping("/{model}/{id}")
    public R<Void> delete(@PathVariable String model, @PathVariable String id) {
        ModelInfo info = getModelInfo(model);
        info.getModel().delete(id);
        return R.ok();
    }

    @DeleteMapping("/{model}/batch")
    public R<Void> deleteBatch(@PathVariable String model, @RequestBody List<String> ids) {
        ModelInfo info = getModelInfo(model);
        info.getModel().deleteBatch(ids);
        return R.ok();
    }

    @PostMapping("/{model}/action/{method}")
    public R<?> actionPost(@PathVariable String model, @PathVariable String method,
                           @RequestBody(required = false) Map<String, Object> params) {
        return invokeMethod(model, method, params);
    }

    @GetMapping("/{model}/action/{method}")
    public R<?> actionGet(@PathVariable String model, @PathVariable String method,
                          @RequestParam(required = false) Map<String, Object> params) {
        return invokeMethod(model, method, params);
    }

    @PostMapping("/{model}/action/{method}/{id}")
    public R<?> actionWithId(@PathVariable String model, @PathVariable String method,
                             @PathVariable String id, @RequestBody(required = false) Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("id", id);
        return invokeMethod(model, method, params);
    }

    @GetMapping("/{model}/export")
    public void export(@PathVariable String model, QueryParams params) {
        ModelInfo info = getModelInfo(model);
        log.info("导出 Model: {}", info.getAnnotation().name());
    }

    @PostMapping("/{model}/import")
    public R<Void> importData(@PathVariable String model) {
        ModelInfo info = getModelInfo(model);
        log.info("导入 Model: {}", info.getAnnotation().name());
        return R.ok();
    }

    @GetMapping("/{model}/meta")
    public R<Map<String, Object>> getMeta(@PathVariable String model) {
        ModelInfo info = getModelInfo(model);
        Map<String, Object> meta = new HashMap<>();
        meta.put("name", info.getAnnotation().name());
        meta.put("table", info.getAnnotation().table());
        meta.put("api", info.getAnnotation().api());
        meta.put("orgIsolation", info.getAnnotation().orgIsolation());
        meta.put("logicDelete", info.getAnnotation().logicDelete());
        meta.put("audit", info.getAnnotation().audit());
        meta.put("sortField", info.getAnnotation().sortField());
        meta.put("sortOrder", info.getAnnotation().sortOrder());
        return R.ok(meta);
    }

    @GetMapping("/_models")
    public R<List<Map<String, Object>>> listModels() {
        List<Map<String, Object>> models = new ArrayList<>();
        for (Map.Entry<String, ModelInfo> entry : modelRegistry.entrySet()) {
            ModelInfo info = entry.getValue();
            Map<String, Object> modelInfo = new HashMap<>();
            modelInfo.put("key", entry.getKey());
            modelInfo.put("name", info.getAnnotation().name());
            modelInfo.put("table", info.getAnnotation().table());
            modelInfo.put("api", info.getAnnotation().api());
            models.add(modelInfo);
        }
        return R.ok(models);
    }

    private ModelInfo getModelInfo(String modelKey) {
        ModelInfo info = modelRegistry.get(modelKey);
        if (info == null) {
            throw new BusinessException("Model 不存在: " + modelKey);
        }
        return info;
    }

    private String getModelKey(Class<?> modelClass) {
        Model annotation = modelClass.getAnnotation(Model.class);
        if (annotation != null && !annotation.api().isEmpty()) {
            return annotation.api();
        }
        String className = modelClass.getSimpleName();
        if (className.endsWith("Model")) {
            return className.substring(0, className.length() - 5).toLowerCase();
        }
        return className.toLowerCase();
    }

    @SuppressWarnings("unchecked")
    private Object convertToEntity(ModelInfo info, Map<String, Object> data) {
        try {
            BaseModel<?> model = info.getModel();
            Class<?> entityClass = model.entityClass;
            Object entity = entityClass.getDeclaredConstructor().newInstance();

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                try {
                    java.lang.reflect.Field field = entityClass.getDeclaredField(fieldName);
                    field.setAccessible(true);

                    if (value != null) {
                        Class<?> fieldType = field.getType();
                        value = convertValue(value, fieldType);
                    }

                    field.set(entity, value);
                } catch (NoSuchFieldException ignored) {
                }
            }

            return entity;
        } catch (Exception e) {
            throw new BusinessException("数据转换失败: " + e.getMessage());
        }
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isInstance(value)) {
            return value;
        }

        String strValue = value.toString();

        if (targetType == String.class) {
            return strValue;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(strValue);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(strValue);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(strValue);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(strValue);
        }

        return objectMapper.convertValue(value, targetType);
    }

    private R<?> invokeMethod(String model, String methodName, Map<String, Object> params) {
        ModelInfo info = getModelInfo(model);
        BaseModel<?> modelInstance = info.getModel();

        try {
            Method targetMethod = findMethod(modelInstance, methodName, params);
            if (targetMethod == null) {
                return R.fail("方法不存在: " + methodName);
            }

            Object[] args = buildMethodArgs(targetMethod, params);

            Object result = targetMethod.invoke(modelInstance, args);
            return R.ok(result);

        } catch (BusinessException e) {
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("调用方法失败: {}.{}", model, methodName, e);
            return R.fail("调用方法失败: " + e.getCause().getMessage());
        }
    }

    private Method findMethod(BaseModel<?> model, String methodName, Map<String, Object> params) {
        for (Method method : model.getClass().getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private Object[] buildMethodArgs(Method method, Map<String, Object> params) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            String paramName = param.getName();
            Class<?> paramType = param.getType();

            if (params != null && params.containsKey(paramName)) {
                args[i] = convertValue(params.get(paramName), paramType);
            } else if (paramType == Map.class) {
                args[i] = params;
            } else if (paramType == List.class) {
                Object value = params.get(paramName);
                if (value instanceof List) {
                    args[i] = value;
                }
            }
        }

        return args;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ModelInfo {
        private BaseModel<?> model;
        private Model annotation;
    }
}
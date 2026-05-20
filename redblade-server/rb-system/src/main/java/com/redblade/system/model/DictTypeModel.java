package com.redblade.system.model;

import com.redblade.auth.annotation.RequiresPermission;
import com.redblade.common.exception.BusinessException;
import com.redblade.common.helper.MasterDaoHelper;
import com.redblade.common.helper.MessageHelper;
import com.redblade.model.annotation.Model;
import com.redblade.model.core.BaseModel;
import com.redblade.system.entity.SysDictType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典类型管理 Model
 *
 * API 接口：
 * - GET    /api/model/dictType/list         分页查询字典类型
 * - GET    /api/model/dictType/all          查询所有字典类型
 * - GET    /api/model/dictType/{id}         查询字典类型详情
 * - POST   /api/model/dictType              新增字典类型
 * - PUT    /api/model/dictType/{id}         修改字典类型
 * - DELETE /api/model/dictType/{id}         删除字典类型
 * - POST   /api/model/dictType/action/updateStatus  修改状态
 */
@Slf4j
@Model(
    name = "字典类型管理",
    table = "sys_dict_type",
    api = "dictType",
    orgIsolation = false,
    logicDelete = true,
    audit = true,
    sortField = "dict_id",
    sortOrder = "asc"
)
public class DictTypeModel extends BaseModel<SysDictType> {

    @Autowired
    private MasterDaoHelper masterDaoHelper;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    protected void beforeInsert(SysDictType entity) {
        // 检查字典类型是否重复
        if (existsByDictType(entity.getDictType())) {
            throw new BusinessException(messageHelper.getAsFormat("dict.type.exists", entity.getDictType()));
        }

        // 设置默认值
        if (entity.getStatus() == null) {
            entity.setStatus("0");
        }

        log.info("新增字典类型: {} ({})", entity.getDictName(), entity.getDictType());
    }

    @Override
    protected void beforeUpdate(Serializable id, SysDictType entity) {
        SysDictType existing = getById(id);
        if (existing == null) {
            throw new BusinessException(messageHelper.get("dict.type.not.exist"));
        }

        // 不能修改字典类型
        entity.setDictType(existing.getDictType());
    }

    @Override
    protected void beforeDelete(Serializable id) {
        // 检查是否有字典数据
        SysDictType dictType = getById(id);
        if (dictType != null && hasDictData(dictType.getDictType())) {
            throw new BusinessException(messageHelper.get("dict.type.has.data"));
        }
    }

    /**
     * 修改状态
     */
    @RequiresPermission("system:dict:edit")
    public void updateStatus(Long dictId, String status) {
        SysDictType dictType = getById(dictId);
        if (dictType == null) {
            throw new BusinessException(messageHelper.get("dict.type.not.exist"));
        }

        dictType.setStatus(status);
        updateById(dictType);

        log.info("修改字典类型状态: {} -> {}", dictType.getDictName(), status);
    }

    /**
     * 获取字典类型下拉选项
     */
    public List<Map<String, Object>> getDictTypeOptions() {
        String sql = "SELECT dict_type AS value, dict_name AS label FROM sys_dict_type " +
                     "WHERE status = '0' AND del_flag = '0' ORDER BY dict_id";
        return masterDaoHelper.select(sql, new HashMap<>());
    }

    /**
     * 检查字典类型是否存在
     */
    private boolean existsByDictType(String dictType) {
        String sql = "SELECT 1 FROM sys_dict_type WHERE dict_type = :dictType AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("dictType", dictType);
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查是否有字典数据
     */
    private boolean hasDictData(String dictType) {
        String sql = "SELECT 1 FROM sys_dict_data WHERE dict_type = :dictType AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("dictType", dictType);
        return masterDaoHelper.hasData(sql, params);
    }
}

package com.redblade.system.model;

import com.redblade.auth.annotation.RequiresPermission;
import com.redblade.common.exception.BusinessException;
import com.redblade.common.helper.MasterDaoHelper;
import com.redblade.common.helper.MessageHelper;
import com.redblade.model.annotation.Model;
import com.redblade.model.core.BaseModel;
import com.redblade.system.entity.SysDictData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典数据管理 Model
 *
 * API 接口：
 * - GET    /api/model/dictData/list         分页查询字典数据
 * - GET    /api/model/dictData/all          查询所有字典数据
 * - GET    /api/model/dictData/{id}         查询字典数据详情
 * - POST   /api/model/dictData              新增字典数据
 * - PUT    /api/model/dictData/{id}         修改字典数据
 * - DELETE /api/model/dictData/{id}         删除字典数据
 * - GET    /api/model/dictData/action/byType  根据类型查询字典数据
 * - POST   /api/model/dictData/action/updateStatus  修改状态
 */
@Slf4j
@Model(
    name = "字典数据管理",
    table = "sys_dict_data",
    api = "dictData",
    orgIsolation = false,
    logicDelete = true,
    audit = true,
    sortField = "dict_sort",
    sortOrder = "asc"
)
public class DictDataModel extends BaseModel<SysDictData> {

    @Autowired
    private MasterDaoHelper masterDaoHelper;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    protected void beforeInsert(SysDictData entity) {
        // 检查字典类型是否存在
        if (!existsDictType(entity.getDictType())) {
            throw new BusinessException(messageHelper.getAsFormat("dict.type.not.exist", entity.getDictType()));
        }

        // 检查同一类型下字典值是否重复
        if (existsByDictValue(entity.getDictType(), entity.getDictValue())) {
            throw new BusinessException(messageHelper.getAsFormat("dict.value.exists", entity.getDictValue()));
        }

        // 设置默认值
        if (entity.getDictSort() == null) {
            entity.setDictSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus("0");
        }
        if (entity.getIsDefault() == null) {
            entity.setIsDefault("N");
        }

        log.info("新增字典数据: {} ({})", entity.getDictLabel(), entity.getDictType());
    }

    @Override
    protected void beforeUpdate(Serializable id, SysDictData entity) {
        SysDictData existing = getById(id);
        if (existing == null) {
            throw new BusinessException(messageHelper.get("dict.data.not.exist"));
        }

        // 不能修改字典类型
        entity.setDictType(existing.getDictType());

        // 检查字典值是否重复（排除自己）
        if (entity.getDictValue() != null && !entity.getDictValue().equals(existing.getDictValue())) {
            if (existsByDictValue(entity.getDictType(), entity.getDictValue())) {
                throw new BusinessException(messageHelper.getAsFormat("dict.value.exists", entity.getDictValue()));
            }
        }
    }

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    public List<Map<String, Object>> getDictDataByType(String dictType) {
        String sql = "SELECT dict_value AS value, dict_label AS label, css_class AS cssClass, " +
                     "list_class AS listClass, is_default AS isDefault " +
                     "FROM sys_dict_data WHERE dict_type = :dictType AND status = '0' AND del_flag = '0' " +
                     "ORDER BY dict_sort";
        Map<String, Object> params = new HashMap<>();
        params.put("dictType", dictType);
        return masterDaoHelper.select(sql, params);
    }

    /**
     * 修改状态
     */
    @RequiresPermission("system:dict:edit")
    public void updateStatus(Long dictCode, String status) {
        SysDictData dictData = getById(dictCode);
        if (dictData == null) {
            throw new BusinessException(messageHelper.get("dict.data.not.exist"));
        }

        dictData.setStatus(status);
        updateById(dictData);

        log.info("修改字典数据状态: {} -> {}", dictData.getDictLabel(), status);
    }

    /**
     * 检查字典类型是否存在
     */
    private boolean existsDictType(String dictType) {
        String sql = "SELECT 1 FROM sys_dict_type WHERE dict_type = :dictType AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("dictType", dictType);
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查字典值是否存在
     */
    private boolean existsByDictValue(String dictType, String dictValue) {
        String sql = "SELECT 1 FROM sys_dict_data WHERE dict_type = :dictType AND dict_value = :dictValue AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("dictType", dictType);
        params.put("dictValue", dictValue);
        return masterDaoHelper.hasData(sql, params);
    }
}

package com.redblade.system.model;

import com.redblade.auth.annotation.RequiresPermission;
import com.redblade.auth.service.AuthService;
import com.redblade.common.exception.BusinessException;
import com.redblade.common.helper.MasterDaoHelper;
import com.redblade.common.helper.MessageHelper;
import com.redblade.model.annotation.Model;
import com.redblade.model.core.BaseModel;
import com.redblade.system.entity.SysDept;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.*;

/**
 * 部门管理 Model
 *
 * API 接口：
 * - GET    /api/model/dept/list         分页查询部门
 * - GET    /api/model/dept/all          查询所有部门
 * - GET    /api/model/dept/{id}         查询部门详情
 * - POST   /api/model/dept              新增部门
 * - PUT    /api/model/dept/{id}         修改部门
 * - DELETE /api/model/dept/{id}         删除部门
 * - GET    /api/model/dept/action/tree  获取部门树
 * - POST   /api/model/dept/action/updateStatus  修改状态
 */
@Slf4j
@Model(
    name = "部门管理",
    table = "sys_dept",
    api = "dept",
    orgIsolation = true,
    logicDelete = true,
    audit = true,
    sortField = "sort",
    sortOrder = "asc"
)
public class DeptModel extends BaseModel<SysDept> {

    @Autowired
    private MasterDaoHelper masterDaoHelper;

    @Autowired
    private MessageHelper messageHelper;

    @Autowired
    private AuthService authService;

    @Override
    protected void beforeInsert(SysDept entity) {
        // 检查部门名称是否重复（同级）
        if (existsByName(entity.getOrgCode(), entity.getParentCode(), entity.getDeptName())) {
            throw new BusinessException(messageHelper.getAsFormat("dept.name.exists", entity.getDeptName()));
        }

        // 设置默认值
        if (entity.getParentCode() == null) {
            entity.setParentCode("");
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus("0");
        }

        // 计算祖级列表
        calculateAncestors(entity);

        log.info("新增部门: {} ({})", entity.getDeptName(), entity.getOrgCode());
    }

    @Override
    protected void beforeUpdate(Serializable id, SysDept entity) {
        SysDept existing = getById(id);
        if (existing == null) {
            throw new BusinessException(messageHelper.get("dept.not.exist"));
        }

        // 检查部门名称是否重复（同级，排除自己）
        if (entity.getDeptName() != null && !entity.getDeptName().equals(existing.getDeptName())) {
            if (existsByName(entity.getOrgCode(), entity.getParentCode(), entity.getDeptName())) {
                throw new BusinessException(messageHelper.getAsFormat("dept.name.exists", entity.getDeptName()));
            }
        }

        // 如果修改了父部门，重新计算祖级列表
        if (entity.getParentCode() != null && !entity.getParentCode().equals(existing.getParentCode())) {
            // 不能把自己设为自己的子部门
            if (isDescendant(entity.getDeptCode(), entity.getParentCode())) {
                throw new BusinessException(messageHelper.get("dept.parent.invalid"));
            }
            calculateAncestors(entity);
        }
    }

    @Override
    protected void beforeDelete(Serializable id) {
        // 检查是否有子部门
        if (hasChildren(id)) {
            throw new BusinessException(messageHelper.get("dept.has.children"));
        }

        // 检查是否有用户
        if (hasUsers(id)) {
            throw new BusinessException(messageHelper.get("dept.has.users"));
        }
    }

    /**
     * 获取部门树
     */
    public List<Map<String, Object>> getDeptTree(String orgCode) {
        String sql = "SELECT * FROM sys_dept WHERE org_code = :orgCode AND del_flag = '0' ORDER BY sort";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        List<Map<String, Object>> depts = masterDaoHelper.select(sql, params);
        return buildTree(depts, "");
    }

    /**
     * 修改状态
     */
    @RequiresPermission("system:dept:edit")
    public void updateStatus(String deptCode, String status) {
        SysDept dept = getById(deptCode);
        if (dept == null) {
            throw new BusinessException(messageHelper.get("dept.not.exist"));
        }

        dept.setStatus(status);
        updateById(dept);

        log.info("修改部门状态: {} -> {}", dept.getDeptName(), status);
    }

    /**
     * 获取部门下拉选项（树形）
     */
    public List<Map<String, Object>> getDeptOptions(String orgCode) {
        String sql = "SELECT dept_code AS id, parent_code AS parentId, dept_name AS label " +
                     "FROM sys_dept WHERE org_code = :orgCode AND del_flag = '0' AND status = '0' ORDER BY sort";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        List<Map<String, Object>> depts = masterDaoHelper.select(sql, params);
        return buildTree(depts, "");
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建树结构
     */
    private List<Map<String, Object>> buildTree(List<Map<String, Object>> depts, String parentCode) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> dept : depts) {
            String deptParentCode = (String) dept.get("parent_code");
            if (deptParentCode == null) {
                deptParentCode = "";
            }
            if (Objects.equals(deptParentCode, parentCode)) {
                String deptCode = (String) dept.get("dept_code");
                Map<String, Object> node = new HashMap<>(dept);
                node.put("children", buildTree(depts, deptCode));
                result.add(node);
            }
        }

        return result;
    }

    /**
     * 计算祖级列表
     */
    private void calculateAncestors(SysDept entity) {
        if (entity.getParentCode() == null || entity.getParentCode().isEmpty()) {
            entity.setAncestors("0");
        } else {
            SysDept parent = getById(entity.getParentCode());
            if (parent == null) {
                throw new BusinessException(messageHelper.get("dept.parent.not.exist"));
            }
            entity.setAncestors(parent.getAncestors() + "," + entity.getParentCode());
        }
    }

    /**
     * 检查部门名称是否存在
     */
    private boolean existsByName(String orgCode, String parentCode, String deptName) {
        String sql = "SELECT 1 FROM sys_dept WHERE org_code = :orgCode AND parent_code = :parentCode " +
                     "AND dept_name = :deptName AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        params.put("parentCode", parentCode == null ? "" : parentCode);
        params.put("deptName", deptName);
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查是否有子部门
     */
    private boolean hasChildren(Serializable deptCode) {
        String sql = "SELECT 1 FROM sys_dept WHERE parent_code = :deptCode AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("deptCode", deptCode.toString());
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查是否有用户
     */
    private boolean hasUsers(Serializable deptCode) {
        String sql = "SELECT 1 FROM sys_user WHERE dept_code = :deptCode AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("deptCode", deptCode.toString());
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查目标部门是否是源部门的后代
     */
    private boolean isDescendant(String sourceCode, String targetCode) {
        if (sourceCode.equals(targetCode)) {
            return true;
        }

        SysDept target = getById(targetCode);
        if (target == null) {
            return false;
        }

        // 检查目标部门的祖级列表是否包含源部门
        return target.getAncestors() != null && target.getAncestors().contains(sourceCode);
    }
}
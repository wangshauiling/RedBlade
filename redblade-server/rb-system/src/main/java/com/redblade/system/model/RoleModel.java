package com.redblade.system.model;

import com.redblade.auth.annotation.RequiresPermission;
import com.redblade.common.exception.BusinessException;
import com.redblade.common.helper.MasterDaoHelper;
import com.redblade.common.helper.MessageHelper;
import com.redblade.model.annotation.Model;
import com.redblade.model.core.BaseModel;
import com.redblade.system.entity.SysRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理 Model
 */
@Slf4j
@Model(
    name = "角色管理",
    table = "sys_role",
    api = "role",
    orgIsolation = true,
    logicDelete = true,
    audit = true,
    sortField = "sort",
    sortOrder = "asc"
)
public class RoleModel extends BaseModel<SysRole> {

    @Autowired
    private MasterDaoHelper masterDaoHelper;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    protected void beforeInsert(SysRole entity) {
        if (existsByRoleCode(entity.getOrgCode(), entity.getRoleCode())) {
            throw new BusinessException(messageHelper.getAsFormat("role.code.exists"));
        }

        if (entity.getStatus() == null) {
            entity.setStatus("0");
        }
        if (entity.getDataScope() == null) {
            entity.setDataScope(2);
        }

        log.info("新增角色: {} ({})", entity.getRoleName(), entity.getOrgCode());
    }

    @Override
    protected void beforeUpdate(Serializable id, SysRole entity) {
        SysRole existing = getById(id);
        if (existing == null) {
            throw new BusinessException(messageHelper.get("role.not.exist"));
        }

        entity.setRoleCode(existing.getRoleCode());
    }

    @Override
    protected void beforeDelete(Serializable id) {
        if (hasUsers(id)) {
            throw new BusinessException(messageHelper.get("role.has.users"));
        }
    }

    @RequiresPermission("system:role:edit")
    public void assignMenu(String roleCode, List<String> menuCodes) {
        SysRole role = getById(roleCode);
        if (role == null) {
            throw new BusinessException(messageHelper.get("role.not.exist"));
        }

        String orgCode = role.getOrgCode();

        String deleteSql = "DELETE FROM sys_role_menu WHERE role_code = :roleCode AND org_code = :orgCode";
        Map<String, Object> params = new HashMap<>();
        params.put("roleCode", roleCode);
        params.put("orgCode", orgCode);
        masterDaoHelper.update(deleteSql, params);

        if (menuCodes != null && !menuCodes.isEmpty()) {
            for (String menuCode : menuCodes) {
                String insertSql = "INSERT INTO sys_role_menu (role_code, menu_code, org_code) VALUES (:roleCode, :menuCode, :orgCode)";
                params.put("menuCode", menuCode);
                masterDaoHelper.update(insertSql, params);
            }
        }

        log.info("分配菜单权限: roleCode={}, menuCodes={}", roleCode, menuCodes);
    }

    public List<String> getRoleMenuCodes(String roleCode) {
        String sql = "SELECT menu_code FROM sys_role_menu WHERE role_code = :roleCode";
        Map<String, Object> params = new HashMap<>();
        params.put("roleCode", roleCode);

        List<Map<String, Object>> rows = masterDaoHelper.select(sql, params);
        return rows.stream()
            .map(row -> (String) row.get("menu_code"))
            .toList();
    }

    @RequiresPermission("system:role:edit")
    public void updateStatus(String roleCode, String status) {
        SysRole role = getById(roleCode);
        if (role == null) {
            throw new BusinessException(messageHelper.get("role.not.exist"));
        }

        role.setStatus(status);
        updateById(role);

        log.info("修改角色状态: {} -> {}", role.getRoleName(), status);
    }

    public List<Map<String, Object>> getRoleOptions(String orgCode) {
        String sql = "SELECT role_code AS value, role_name AS label FROM sys_role " +
                     "WHERE org_code = :orgCode AND status = '0' AND del_flag = '0' ORDER BY sort";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        return masterDaoHelper.select(sql, params);
    }

    private boolean existsByRoleCode(String orgCode, String roleCode) {
        String sql = "SELECT 1 FROM sys_role WHERE org_code = :orgCode AND role_code = :roleCode AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        params.put("roleCode", roleCode);
        return masterDaoHelper.hasData(sql, params);
    }

    private boolean hasUsers(Serializable roleCode) {
        String sql = "SELECT 1 FROM sys_user_role WHERE role_code = :roleCode";
        Map<String, Object> params = new HashMap<>();
        params.put("roleCode", roleCode.toString());
        return masterDaoHelper.hasData(sql, params);
    }
}
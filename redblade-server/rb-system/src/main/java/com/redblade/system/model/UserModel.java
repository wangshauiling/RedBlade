package com.redblade.system.model;

import com.redblade.auth.annotation.RequiresPermission;
import com.redblade.auth.service.AuthService;
import com.redblade.common.exception.BusinessException;
import com.redblade.common.helper.MasterDaoHelper;
import com.redblade.common.helper.MessageHelper;
import com.redblade.model.annotation.Model;
import com.redblade.model.core.BaseModel;
import com.redblade.system.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理 Model
 */
@Slf4j
@Model(
    name = "用户管理",
    table = "sys_user",
    api = "user",
    orgIsolation = true,
    logicDelete = true,
    audit = true,
    sortField = "create_time",
    sortOrder = "desc"
)
public class UserModel extends BaseModel<SysUser> {

    @Autowired
    private MasterDaoHelper masterDaoHelper;

    @Autowired
    private MessageHelper messageHelper;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void beforeInsert(SysUser entity) {
        if (existsByUsername(entity.getOrgCode(), entity.getUsername())) {
            throw new BusinessException(messageHelper.getAsFormat("user.username.exists", entity.getUsername()));
        }

        if (entity.getEmail() != null && existsByEmail(entity.getOrgCode(), entity.getEmail())) {
            throw new BusinessException(messageHelper.getAsFormat("user.email.exists", entity.getEmail()));
        }

        entity.setPassword(passwordEncoder.encode(entity.getPassword()));

        if (entity.getStatus() == null) {
            entity.setStatus("0");
        }

        log.info("新增用户: {} ({})", entity.getUsername(), entity.getOrgCode());
    }

    @Override
    protected void beforeUpdate(Serializable id, SysUser entity) {
        SysUser existing = getById(id);
        if (existing == null) {
            throw new BusinessException(messageHelper.get("user.not.exist"));
        }

        entity.setUsername(existing.getUsername());

        if (entity.getEmail() != null && !entity.getEmail().equals(existing.getEmail())) {
            if (existsByEmail(entity.getOrgCode(), entity.getEmail())) {
                throw new BusinessException(messageHelper.getAsFormat("user.email.exists", entity.getEmail()));
            }
        }

        if (entity.getPassword() == null || entity.getPassword().isEmpty()) {
            entity.setPassword(existing.getPassword());
        } else {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
    }

    @Override
    protected void beforeDelete(Serializable id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException(messageHelper.get("user.not.exist"));
        }

        if ("admin".equals(user.getUsername())) {
            throw new BusinessException(messageHelper.get("user.admin.cannot.delete"));
        }

        String currentUserCode = authService.getCurrentUserCode();
        if (currentUserCode != null && currentUserCode.equals(id)) {
            throw new BusinessException("不能删除自己");
        }
    }

    @RequiresPermission("system:user:resetPwd")
    public void resetPassword(String userCode, String newPassword) {
        SysUser user = getById(userCode);
        if (user == null) {
            throw new BusinessException(messageHelper.get("user.not.exist"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);

        log.info("重置用户密码: {} ({})", user.getUsername(), user.getOrgCode());
    }

    @RequiresPermission("system:user:edit")
    public void updateStatus(String userCode, String status) {
        SysUser user = getById(userCode);
        if (user == null) {
            throw new BusinessException(messageHelper.get("user.not.exist"));
        }

        if ("admin".equals(user.getUsername()) && "1".equals(status)) {
            throw new BusinessException("不能禁用管理员账号");
        }

        user.setStatus(status);
        updateById(user);

        log.info("修改用户状态: {} -> {}", user.getUsername(), status);
    }

    @RequiresPermission("system:user:edit")
    public void assignRole(String userCode, List<String> roleCodes) {
        SysUser user = getById(userCode);
        if (user == null) {
            throw new BusinessException(messageHelper.get("user.not.exist"));
        }

        String orgCode = user.getOrgCode();

        String deleteSql = "DELETE FROM sys_user_role WHERE user_code = :userCode AND org_code = :orgCode";
        Map<String, Object> params = new HashMap<>();
        params.put("userCode", userCode);
        params.put("orgCode", orgCode);
        masterDaoHelper.update(deleteSql, params);

        if (roleCodes != null && !roleCodes.isEmpty()) {
            for (String roleCode : roleCodes) {
                String insertSql = "INSERT INTO sys_user_role (user_code, role_code, org_code) VALUES (:userCode, :roleCode, :orgCode)";
                params.put("roleCode", roleCode);
                masterDaoHelper.update(insertSql, params);
            }
        }

        log.info("分配角色: userCode={}, roleCodes={}", userCode, roleCodes);
    }

    public List<String> getUserRoleCodes(String userCode) {
        String sql = "SELECT role_code FROM sys_user_role WHERE user_code = :userCode";
        Map<String, Object> params = new HashMap<>();
        params.put("userCode", userCode);

        List<Map<String, Object>> rows = masterDaoHelper.select(sql, params);
        return rows.stream()
            .map(row -> (String) row.get("role_code"))
            .toList();
    }

    public List<Map<String, Object>> getUserOptions() {
        String orgCode = authService.getCurrentOrgCode();
        String sql = "SELECT user_code AS value, nickname AS label FROM sys_user " +
                     "WHERE org_code = :orgCode AND status = '0' AND del_flag = '0' ORDER BY create_time DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        return masterDaoHelper.select(sql, params);
    }

    private boolean existsByUsername(String orgCode, String username) {
        String sql = "SELECT 1 FROM sys_user WHERE org_code = :orgCode AND username = :username AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        params.put("username", username);
        return masterDaoHelper.hasData(sql, params);
    }

    private boolean existsByEmail(String orgCode, String email) {
        String sql = "SELECT 1 FROM sys_user WHERE org_code = :orgCode AND email = :email AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        params.put("email", email);
        return masterDaoHelper.hasData(sql, params);
    }
}
package com.redblade.system.model;

import com.redblade.auth.annotation.RequiresPermission;
import com.redblade.auth.service.AuthService;
import com.redblade.common.exception.BusinessException;
import com.redblade.common.helper.MasterDaoHelper;
import com.redblade.common.helper.MessageHelper;
import com.redblade.model.annotation.Model;
import com.redblade.model.core.BaseModel;
import com.redblade.system.entity.SysOrg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 组织管理 Model
 *
 * API 接口：
 * - GET    /api/model/org/list         分页查询组织
 * - GET    /api/model/org/all          查询所有组织
 * - GET    /api/model/org/{id}         查询组织详情
 * - POST   /api/model/org              新增组织
 * - PUT    /api/model/org/{id}         修改组织
 * - DELETE /api/model/org/{id}         删除组织
 * - GET    /api/model/org/action/tree  获取组织树
 * - POST   /api/model/org/action/updateStatus  修改状态
 */
@Slf4j
@Model(
    name = "组织管理",
    table = "sys_org",
    api = "org",
    orgIsolation = false,  // 组织本身不需要隔离
    logicDelete = true,
    audit = true,
    sortField = "sort",
    sortOrder = "asc"
)
public class OrgModel extends BaseModel<SysOrg> {

    @Autowired
    private MasterDaoHelper masterDaoHelper;

    @Autowired
    private MessageHelper messageHelper;

    @Autowired
    private AuthService authService;

    // ==================== CRUD 钩子方法 ====================

    @Override
    protected void beforeInsert(SysOrg entity) {
        // 检查组织编码是否重复
        if (existsByOrgCode(entity.getOrgCode())) {
            throw new BusinessException(messageHelper.getAsFormat("org.code.exists", entity.getOrgCode()));
        }

        // 检查组织名称是否重复（同级）
        if (existsByName(entity.getParentCode(), entity.getOrgName())) {
            throw new BusinessException(messageHelper.getAsFormat("org.name.exists", entity.getOrgName()));
        }

        // 设置默认值
        if (entity.getParentCode() == null || entity.getParentCode().isEmpty()) {
            entity.setParentCode("ROOT");
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus("0");
        }

        // 计算组织层级和路径
        calculateOrgPath(entity);

        log.info("新增组织: {} ({})", entity.getOrgName(), entity.getOrgCode());
    }

    // 注意：SysOrg 主键是 String 类型，不支持 beforeUpdate/beforeDelete 钩子
    // 请使用 updateOrg 和 deleteOrg 自定义方法

    // ==================== 自定义业务方法 ====================

    /**
     * 修改组织
     */
    @RequiresPermission("system:org:edit")
    public void updateOrg(String orgCode, SysOrg entity) {
        SysOrg existing = getById(orgCode);
        if (existing == null) {
            throw new BusinessException(messageHelper.get("org.not.exist"));
        }

        // 不能修改组织编码
        entity.setOrgCode(existing.getOrgCode());

        // 检查组织名称是否重复（同级，排除自己）
        if (entity.getOrgName() != null && !entity.getOrgName().equals(existing.getOrgName())) {
            if (existsByName(entity.getParentCode(), entity.getOrgName())) {
                throw new BusinessException(messageHelper.getAsFormat("org.name.exists", entity.getOrgName()));
            }
        }

        // 如果修改了父组织，重新计算路径
        if (entity.getParentCode() != null && !entity.getParentCode().equals(existing.getParentCode())) {
            // 不能把自己设为自己的子组织
            if (isDescendant(entity.getOrgCode(), entity.getParentCode())) {
                throw new BusinessException(messageHelper.get("org.parent.invalid"));
            }
            calculateOrgPath(entity);
        }

        updateById(entity);
        log.info("修改组织: {}", entity.getOrgName());
    }

    /**
     * 删除组织
     */
    @RequiresPermission("system:org:delete")
    public void deleteOrg(String orgCode) {
        // 检查是否有子组织
        if (hasChildren(orgCode)) {
            throw new BusinessException(messageHelper.get("org.has.children"));
        }

        // 检查是否有用户
        if (hasUsers(orgCode)) {
            throw new BusinessException(messageHelper.get("org.has.users"));
        }

        // 检查是否有角色
        if (hasRoles(orgCode)) {
            throw new BusinessException(messageHelper.get("org.has.roles"));
        }

        removeById(orgCode);
        log.info("删除组织: {}", orgCode);
    }

    /**
     * 获取组织树
     *
     * @return 组织树结构
     */
    public List<Map<String, Object>> getOrgTree() {
        String sql = "SELECT * FROM sys_org WHERE del_flag = '0' ORDER BY sort";
        List<Map<String, Object>> orgs = masterDaoHelper.select(sql, new HashMap<>());
        return buildTree(orgs, "ROOT");
    }

    /**
     * 获取用户可管理的组织树
     *
     * @param userId 用户ID
     * @return 组织树
     */
    public List<Map<String, Object>> getUserOrgTree(Long userId) {
        // 获取用户的数据权限范围
        // TODO: 根据用户的数据权限过滤组织
        return getOrgTree();
    }

    /**
     * 修改状态
     */
    @RequiresPermission("system:org:edit")
    public void updateStatus(String orgCode, String status) {
        SysOrg org = getById(orgCode);
        if (org == null) {
            throw new BusinessException(messageHelper.get("org.not.exist"));
        }

        org.setStatus(status);
        updateById(org);

        log.info("修改组织状态: {} -> {}", org.getOrgName(), status);
    }

    /**
     * 获取组织下拉选项（树形）
     */
    public List<Map<String, Object>> getOrgOptions() {
        String sql = "SELECT org_code AS id, parent_code AS parentId, org_name AS label " +
                     "FROM sys_org WHERE del_flag = '0' AND status = '0' ORDER BY sort";
        List<Map<String, Object>> orgs = masterDaoHelper.select(sql, new HashMap<>());
        return buildTree(orgs, "ROOT");
    }

    /**
     * 获取组织及其所有子组织编码
     *
     * @param orgCode 组织编码
     * @return 组织编码列表
     */
    public List<String> getOrgAndChildren(String orgCode) {
        String sql = "SELECT org_code FROM sys_org WHERE org_path LIKE :pathPrefix AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();

        SysOrg org = getById(orgCode);
        if (org == null) {
            return Collections.singletonList(orgCode);
        }

        params.put("pathPrefix", org.getOrgPath() + "%");
        List<Map<String, Object>> rows = masterDaoHelper.select(sql, params);
        return rows.stream()
            .map(row -> (String) row.get("org_code"))
            .toList();
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建树结构
     */
    private List<Map<String, Object>> buildTree(List<Map<String, Object>> orgs, String parentCode) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> org : orgs) {
            String orgParentCode = (String) org.get("parent_code");
            if (Objects.equals(orgParentCode, parentCode)) {
                String orgCode = (String) org.get("org_code");
                Map<String, Object> node = new HashMap<>(org);
                node.put("children", buildTree(orgs, orgCode));
                result.add(node);
            }
        }

        return result;
    }

    /**
     * 计算组织层级和路径
     */
    private void calculateOrgPath(SysOrg entity) {
        if ("ROOT".equals(entity.getParentCode()) || entity.getParentCode() == null) {
            entity.setOrgLevel(1);
            entity.setOrgPath(entity.getOrgCode());
        } else {
            SysOrg parent = getById(entity.getParentCode());
            if (parent == null) {
                throw new BusinessException(messageHelper.get("org.parent.not.exist"));
            }
            entity.setOrgLevel(parent.getOrgLevel() + 1);
            entity.setOrgPath(parent.getOrgPath() + "." + entity.getOrgCode());
        }
    }

    /**
     * 检查组织编码是否存在
     */
    private boolean existsByOrgCode(String orgCode) {
        String sql = "SELECT 1 FROM sys_org WHERE org_code = :orgCode AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查组织名称是否存在
     */
    private boolean existsByName(String parentCode, String orgName) {
        String sql = "SELECT 1 FROM sys_org WHERE parent_code = :parentCode AND org_name = :orgName AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("parentCode", parentCode == null ? "ROOT" : parentCode);
        params.put("orgName", orgName);
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查是否有子组织
     */
    private boolean hasChildren(String orgCode) {
        String sql = "SELECT 1 FROM sys_org WHERE parent_code = :orgCode AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查是否有用户
     */
    private boolean hasUsers(String orgCode) {
        String sql = "SELECT 1 FROM sys_user WHERE org_code = :orgCode AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查是否有角色
     */
    private boolean hasRoles(String orgCode) {
        String sql = "SELECT 1 FROM sys_role WHERE org_code = :orgCode AND del_flag = '0'";
        Map<String, Object> params = new HashMap<>();
        params.put("orgCode", orgCode);
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查目标组织是否是源组织的后代
     */
    private boolean isDescendant(String sourceCode, String targetCode) {
        if (sourceCode.equals(targetCode)) {
            return true;
        }

        SysOrg target = getById(targetCode);
        if (target == null) {
            return false;
        }

        // 检查目标组织的路径是否包含源组织
        return target.getOrgPath() != null && target.getOrgPath().contains(sourceCode);
    }
}

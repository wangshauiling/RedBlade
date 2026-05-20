package com.redblade.system.model;

import com.redblade.auth.annotation.RequiresPermission;
import com.redblade.auth.service.AuthService;
import com.redblade.common.exception.BusinessException;
import com.redblade.common.helper.MasterDaoHelper;
import com.redblade.common.helper.MessageHelper;
import com.redblade.model.annotation.Model;
import com.redblade.model.core.BaseModel;
import com.redblade.system.entity.SysMenu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.*;

/**
 * 菜单管理 Model
 *
 * API 接口：
 * - GET    /api/model/menu/list         分页查询菜单
 * - GET    /api/model/menu/all          查询所有菜单
 * - GET    /api/model/menu/{id}         查询菜单详情
 * - POST   /api/model/menu              新增菜单
 * - PUT    /api/model/menu/{id}         修改菜单
 * - DELETE /api/model/menu/{id}         删除菜单
 * - GET    /api/model/menu/action/tree  获取菜单树
 * - POST   /api/model/menu/action/updateStatus  修改状态
 */
@Slf4j
@Model(
    name = "菜单管理",
    table = "sys_menu",
    api = "menu",
    orgIsolation = false,
    logicDelete = false,
    audit = true,
    sortField = "sort",
    sortOrder = "asc"
)
public class MenuModel extends BaseModel<SysMenu> {

    @Autowired
    private MasterDaoHelper masterDaoHelper;

    @Autowired
    private MessageHelper messageHelper;

    @Autowired
    private AuthService authService;

    // ==================== CRUD 钩子方法 ====================

    @Override
    protected void beforeInsert(SysMenu entity) {
        // 检查菜单名称是否重复（同级）
        if (existsByName(entity.getParentCode(), entity.getMenuName())) {
            throw new BusinessException(messageHelper.getAsFormat("menu.name.exists", entity.getMenuName()));
        }

        // 检查权限标识是否重复
        if (entity.getPermission() != null && existsByPermission(entity.getPermission())) {
            throw new BusinessException(messageHelper.getAsFormat("menu.permission.exists", entity.getPermission()));
        }

        // 设置默认值
        if (entity.getParentCode() == null) {
            entity.setParentCode("");
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getVisible() == null) {
            entity.setVisible("0");
        }
        if (entity.getStatus() == null) {
            entity.setStatus("0");
        }

        log.info("新增菜单: {} ({})", entity.getMenuName(), entity.getMenuCode());
    }

    @Override
    protected void beforeUpdate(Serializable id, SysMenu entity) {
        SysMenu existing = getById(id);
        if (existing == null) {
            throw new BusinessException(messageHelper.get("menu.not.exist"));
        }

        // 检查菜单名称是否重复（同级，排除自己）
        if (entity.getMenuName() != null && !entity.getMenuName().equals(existing.getMenuName())) {
            if (existsByName(entity.getParentCode(), entity.getMenuName())) {
                throw new BusinessException(messageHelper.getAsFormat("menu.name.exists", entity.getMenuName()));
            }
        }

        // 检查权限标识是否重复（排除自己）
        if (entity.getPermission() != null && !entity.getPermission().equals(existing.getPermission())) {
            if (existsByPermission(entity.getPermission())) {
                throw new BusinessException(messageHelper.getAsFormat("menu.permission.exists", entity.getPermission()));
            }
        }
    }

    @Override
    protected void beforeDelete(Serializable id) {
        // 检查是否有子菜单
        if (hasChildren(id)) {
            throw new BusinessException(messageHelper.get("menu.has.children"));
        }

        // 检查是否已分配给角色
        if (hasRoles(id)) {
            throw new BusinessException(messageHelper.get("menu.has.roles"));
        }
    }

    // ==================== 自定义业务方法 ====================

    /**
     * 获取菜单树
     *
     * @return 菜单树结构
     */
    public List<Map<String, Object>> getMenuTree() {
        String sql = "SELECT * FROM sys_menu ORDER BY sort";
        List<Map<String, Object>> menus = masterDaoHelper.select(sql, new HashMap<>());
        return buildTree(menus, "");
    }

    /**
     * 获取用户菜单树（根据权限过滤）
     *
     * @param userCode 用户编码
     * @return 菜单树
     */
    public List<Map<String, Object>> getUserMenuTree(String userCode) {
        String sql = "SELECT DISTINCT m.* FROM sys_menu m " +
                     "INNER JOIN sys_role_menu rm ON m.menu_code = rm.menu_code " +
                     "INNER JOIN sys_user_role ur ON rm.role_code = ur.role_code " +
                     "WHERE ur.user_code = :userCode " +
                     "AND m.status = '0' " +
                     "ORDER BY m.sort";

        Map<String, Object> params = new HashMap<>();
        params.put("userCode", userCode);

        List<Map<String, Object>> menus = masterDaoHelper.select(sql, params);
        return buildTree(menus, "");
    }

    /**
     * 修改状态
     */
    @RequiresPermission("system:menu:edit")
    public void updateStatus(String menuCode, String status) {
        SysMenu menu = getById(menuCode);
        if (menu == null) {
            throw new BusinessException(messageHelper.get("menu.not.exist"));
        }

        menu.setStatus(status);
        updateById(menu);

        log.info("修改菜单状态: {} -> {}", menu.getMenuName(), status);
    }

    /**
     * 获取菜单下拉选项（树形）
     */
    public List<Map<String, Object>> getMenuOptions() {
        String sql = "SELECT menu_code AS id, parent_code AS parentId, menu_name AS label " +
                     "FROM sys_menu WHERE menu_type IN ('M', 'C') ORDER BY sort";

        List<Map<String, Object>> menus = masterDaoHelper.select(sql, new HashMap<>());
        return buildTree(menus, "");
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建树结构
     */
    private List<Map<String, Object>> buildTree(List<Map<String, Object>> menus, String parentCode) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> menu : menus) {
            String menuParentCode = (String) menu.get("parent_code");
            if (menuParentCode == null) {
                menuParentCode = "";
            }
            if (Objects.equals(menuParentCode, parentCode)) {
                String menuCode = (String) menu.get("menu_code");
                Map<String, Object> node = new HashMap<>(menu);
                node.put("children", buildTree(menus, menuCode));
                result.add(node);
            }
        }

        return result;
    }

    /**
     * 检查菜单名称是否存在
     */
    private boolean existsByName(String parentCode, String menuName) {
        String sql = "SELECT 1 FROM sys_menu WHERE parent_code = :parentCode " +
                     "AND menu_name = :menuName";
        Map<String, Object> params = new HashMap<>();
        params.put("parentCode", parentCode == null ? "" : parentCode);
        params.put("menuName", menuName);
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查权限标识是否存在
     */
    private boolean existsByPermission(String permission) {
        String sql = "SELECT 1 FROM sys_menu WHERE permission = :permission";
        Map<String, Object> params = new HashMap<>();
        params.put("permission", permission);
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查是否有子菜单
     */
    private boolean hasChildren(Serializable menuCode) {
        String sql = "SELECT 1 FROM sys_menu WHERE parent_code = :menuCode";
        Map<String, Object> params = new HashMap<>();
        params.put("menuCode", menuCode.toString());
        return masterDaoHelper.hasData(sql, params);
    }

    /**
     * 检查是否已分配给角色
     */
    private boolean hasRoles(Serializable menuCode) {
        String sql = "SELECT 1 FROM sys_role_menu WHERE menu_code = :menuCode";
        Map<String, Object> params = new HashMap<>();
        params.put("menuCode", menuCode.toString());
        return masterDaoHelper.hasData(sql, params);
    }
}
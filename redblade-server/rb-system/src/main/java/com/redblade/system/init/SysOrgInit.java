package com.redblade.system.init;

import com.redblade.init.metadata.annotation.DbMetaData;
import com.redblade.init.metadata.domain.BaseDml;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统组织初始化数据
 */
@DbMetaData(
    table = "sys_org",
    order = 5,
    idempotent = true,
    uniqueKeys = {"org_code"},
    description = "系统默认组织"
)
public class SysOrgInit extends BaseDml<Map<String, Object>> {

    public SysOrgInit() {
        // 根组织
        Map<String, Object> root = new HashMap<>();
        root.put("org_code", "001");
        root.put("parent_code", null);
        root.put("org_name", "RedBlade");
        root.put("org_type", "hq");
        root.put("org_level", 1);
        root.put("org_path", "ROOT");
        root.put("sort", 0);
        root.put("status", "0");
        root.put("del_flag", "0");
        add(root);
    }
}

package com.redblade.system.init;

import com.redblade.init.metadata.annotation.DbMetaData;
import com.redblade.init.metadata.domain.BaseDml;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统用户初始化数据
 */
@DbMetaData(
    table = "sys_user",
    order = 20,
    idempotent = true,
    uniqueKeys = {"org_code", "username"},
    description = "系统默认用户"
)
public class SysUserInit extends BaseDml<Map<String, Object>> {

    public SysUserInit() {
        // 管理员用户
        Map<String, Object> admin = new HashMap<>();
        admin.put("org_code", "001");
        admin.put("user_code", "admin");
        admin.put("username", "admin");
        admin.put("password", Base64.getEncoder().encodeToString("123456".getBytes())); // Base64编码: MTIzNDU2
        admin.put("nickname", "超级管理员");
        admin.put("gender", "0");
        admin.put("status", "0");
        admin.put("del_flag", "0");
        add(admin);
    }
}

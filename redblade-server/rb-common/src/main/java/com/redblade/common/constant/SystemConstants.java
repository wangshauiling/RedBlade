package com.redblade.common.constant;

/**
 * 系统常量
 */
public interface SystemConstants {

    /**
     * 正常状态
     */
    String NORMAL = "0";

    /**
     * 异常状态
     */
    String DISABLE = "1";

    /**
     * 删除标志 - 正常
     */
    String DEL_FLAG_NORMAL = "0";

    /**
     * 删除标志 - 已删除
     */
    String DEL_FLAG_DELETED = "1";

    /**
     * 超级管理员ID
     */
    Long SUPER_ADMIN_ID = 1L;

    /**
     * 默认页码
     */
    int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页数量
     */
    int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页数量
     */
    int MAX_PAGE_SIZE = 100;
}
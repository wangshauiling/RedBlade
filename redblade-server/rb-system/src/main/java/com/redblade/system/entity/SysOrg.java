package com.redblade.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织实体
 */
@Data
@TableName("sys_org")
public class SysOrg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组织编码（主键）
     */
    @TableId(type = IdType.INPUT)
    private String orgCode;

    /**
     * 父组织编码
     */
    private String parentCode;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 组织类型（hq总部 company分公司 dept部门 team项目组）
     */
    private String orgType;

    /**
     * 组织层级
     */
    private Integer orgLevel;

    /**
     * 组织路径（如：ROOT.COMPANY1.DEPT1）
     */
    private String orgPath;

    /**
     * 负责人ID
     */
    private Long leaderId;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标志（0正常 1删除）
     */
    @TableLogic
    private String delFlag;
}
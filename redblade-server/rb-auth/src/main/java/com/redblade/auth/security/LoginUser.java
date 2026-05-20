package com.redblade.auth.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 登录用户信息（含组织字段）
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 组织编码
     */
    private String orgCode;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 组织类型
     */
    private String orgType;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 角色列表
     */
    private Set<String> roles;

    /**
     * 数据权限范围（1全部 2自定义 3本组织 4本组织及以下 5仅本人）
     */
    private String dataScope;

    /**
     * 本组织及以下组织编码列表（用于数据权限过滤）
     */
    private List<String> orgCodes;

    /**
     * 自定义数据权限组织编码列表
     */
    private List<String> customOrgCodes;

    /**
     * AccessToken
     */
    private String accessToken;

    /**
     * RefreshToken
     */
    private String refreshToken;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return "0".equals(this.status);
    }
}
package com.redblade.auth.service.impl;

import com.redblade.auth.domain.ChangePasswordRequest;
import com.redblade.auth.domain.LoginRequest;
import com.redblade.auth.domain.LoginResponse;
import com.redblade.auth.domain.RegisterRequest;
import com.redblade.auth.jwt.JwtUtils;
import com.redblade.auth.security.LoginUser;
import com.redblade.auth.service.AuthService;
import com.redblade.auth.service.CaptchaService;
import com.redblade.common.constant.CacheConstants;
import com.redblade.common.exception.BusinessException;
import com.redblade.common.helper.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final MessageHelper messageHelper;
    private final CaptchaService captchaService;

    // Token 过期时间配置
    private static final long ACCESS_TOKEN_EXPIRATION = 7200; // 2小时（秒）
    private static final long REFRESH_TOKEN_EXPIRATION = 604800; // 7天（秒）

    // 登录失败限制配置
    private static final int MAX_LOGIN_FAIL_COUNT = 5; // 最大失败次数
    private static final long LOGIN_LOCK_TIME = 30; // 锁定时间（分钟）

    @Override
    public LoginResponse login(LoginRequest request) {
        String loginKey = request.getOrgCode() + ":" + request.getUsername();

        // 0. 检查是否被锁定
        String lockKey = CacheConstants.USER_LOCK_KEY + loginKey;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            Long remainTime = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            throw new BusinessException(messageHelper.getAsFormat("user.locked.wait", remainTime));
        }

        // 1. 查询用户
        String sql = "SELECT user_code, username, password, nickname, status, org_code, dept_code " +
                     "FROM sys_user WHERE org_code = ? AND username = ? AND del_flag = '0'";
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, request.getOrgCode(), request.getUsername());

        if (userList.isEmpty()) {
            recordLoginFail(loginKey);
            throw new BusinessException(messageHelper.get("user.not.exist"));
        }
        Map<String, Object> userMap = userList.get(0);

        // 2. 验证密码（Base64编码）
        String encodedPassword = (String) userMap.get("password");
        if (!validatePassword(request.getPassword(), encodedPassword)) {
            recordLoginFail(loginKey);
            throw new BusinessException(messageHelper.get("user.password.error"));
        }

        // 3. 检查状态
        String status = (String) userMap.get("status");
        if ("1".equals(status)) {
            throw new BusinessException(messageHelper.get("user.disabled"));
        }

        // 登录成功，清除失败记录
        clearLoginFailRecord(loginKey);

        String userCode = (String) userMap.get("user_code");
        String username = (String) userMap.get("username");
        String orgCode = (String) userMap.get("org_code");

        // 4. 查询组织信息
        String orgName = getOrgName(orgCode);

        // 5. 查询权限
        Set<String> permissions = getUserPermissions(userCode, orgCode);
        Set<String> roles = getUserRoles(userCode, orgCode);

        // 5.1 查询数据权限
        String dataScope = getUserDataScope(userCode, orgCode);
        List<String> orgCodes = getOrgCodes(orgCode);
        List<String> customOrgCodes = getCustomOrgCodes(userCode, orgCode);

        // 6. 生成 Token
        String accessToken = jwtUtils.generateAccessToken(userCode, username);
        String refreshToken = jwtUtils.generateRefreshToken(userCode, username);

        // 7. 构建 LoginUser
        LoginUser loginUser = new LoginUser();
        loginUser.setUserCode(userCode);
        loginUser.setUsername(username);
        loginUser.setNickname((String) userMap.get("nickname"));
        loginUser.setOrgCode(orgCode);
        loginUser.setOrgName(orgName);
        loginUser.setDeptCode((String) userMap.get("dept_code"));
        loginUser.setStatus(status);
        loginUser.setPermissions(permissions);
        loginUser.setRoles(roles);
        loginUser.setDataScope(dataScope);
        loginUser.setOrgCodes(orgCodes);
        loginUser.setCustomOrgCodes(customOrgCodes);
        loginUser.setAccessToken(accessToken);
        loginUser.setRefreshToken(refreshToken);

        // 8. 存入 Redis
        String cacheKey = CacheConstants.LOGIN_TOKEN_KEY + accessToken;
        redisTemplate.opsForValue().set(cacheKey, loginUser, ACCESS_TOKEN_EXPIRATION, TimeUnit.SECONDS);

        log.info("用户登录成功: {} ({})", username, orgCode);

        // 9. 返回响应
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(ACCESS_TOKEN_EXPIRATION)
            .userCode(userCode)
            .username(username)
            .nickname((String) userMap.get("nickname"))
            .orgCode(orgCode)
            .orgName(orgName)
            .build();
    }

    @Override
    public void logout() {
        LoginUser loginUser = getCurrentUser();
        if (loginUser != null) {
            String cacheKey = CacheConstants.LOGIN_TOKEN_KEY + loginUser.getAccessToken();
            redisTemplate.delete(cacheKey);
            log.info("用户登出成功: {}", loginUser.getUsername());
        }
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        // 1. 验证 RefreshToken
        if (!jwtUtils.validateToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
            throw new BusinessException(messageHelper.get("user.token.invalid"));
        }

        // 2. 获取用户信息
        String userCode = jwtUtils.getUserCode(refreshToken);
        String username = jwtUtils.getUsername(refreshToken);

        // 3. 查询用户信息
        String sql = "SELECT user_code, username, nickname, status, org_code, dept_code " +
                     "FROM sys_user WHERE user_code = ? AND del_flag = '0'";
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, userCode);

        if (userList.isEmpty()) {
            throw new BusinessException(messageHelper.get("user.not.exist"));
        }
        Map<String, Object> userMap = userList.get(0);

        // 4. 检查状态
        if ("1".equals(userMap.get("status"))) {
            throw new BusinessException(messageHelper.get("user.disabled"));
        }

        String orgCode = (String) userMap.get("org_code");

        // 5. 查询组织信息
        String orgName = getOrgName(orgCode);

        // 6. 查询权限
        Set<String> permissions = getUserPermissions(userCode, orgCode);
        Set<String> roles = getUserRoles(userCode, orgCode);

        // 6.1 查询数据权限
        String dataScope = getUserDataScope(userCode, orgCode);
        List<String> orgCodes = getOrgCodes(orgCode);
        List<String> customOrgCodes = getCustomOrgCodes(userCode, orgCode);

        // 7. 生成新 Token
        String newAccessToken = jwtUtils.generateAccessToken(userCode, username);
        String newRefreshToken = jwtUtils.generateRefreshToken(userCode, username);

        // 8. 构建 LoginUser
        LoginUser loginUser = new LoginUser();
        loginUser.setUserCode(userCode);
        loginUser.setUsername(username);
        loginUser.setNickname((String) userMap.get("nickname"));
        loginUser.setOrgCode(orgCode);
        loginUser.setOrgName(orgName);
        loginUser.setDeptCode((String) userMap.get("dept_code"));
        loginUser.setStatus((String) userMap.get("status"));
        loginUser.setPermissions(permissions);
        loginUser.setRoles(roles);
        loginUser.setDataScope(dataScope);
        loginUser.setOrgCodes(orgCodes);
        loginUser.setCustomOrgCodes(customOrgCodes);
        loginUser.setAccessToken(newAccessToken);
        loginUser.setRefreshToken(newRefreshToken);

        // 9. 存入 Redis
        String cacheKey = CacheConstants.LOGIN_TOKEN_KEY + newAccessToken;
        redisTemplate.opsForValue().set(cacheKey, loginUser, ACCESS_TOKEN_EXPIRATION, TimeUnit.SECONDS);

        log.info("刷新Token成功: {}", username);

        // 10. 返回响应
        return LoginResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .expiresIn(ACCESS_TOKEN_EXPIRATION)
            .userCode(userCode)
            .username(username)
            .nickname((String) userMap.get("nickname"))
            .orgCode(orgCode)
            .orgName(orgName)
            .build();
    }

    @Override
    public LoginUser getCurrentUser() {
        // 从 SecurityContext 获取
        org.springframework.security.core.Authentication authentication =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    @Override
    public Long getCurrentUserId() {
        LoginUser user = getCurrentUser();
        return user != null ? Long.parseLong(user.getUserCode()) : null;
    }

    @Override
    public String getCurrentUserCode() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUserCode() : null;
    }

    @Override
    public String getCurrentOrgCode() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getOrgCode() : null;
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        // 1. 验证码校验
        if (captchaService.isCaptchaEnabled()) {
            if (!captchaService.validateCaptcha(request.getCaptchaKey(), request.getCaptcha())) {
                throw new BusinessException(messageHelper.get("captcha.error"));
            }
        }

        // 2. 检查组织是否存在
        String orgSql = "SELECT 1 FROM sys_org WHERE org_code = ? AND del_flag = '0'";
        List<Map<String, Object>> orgList = jdbcTemplate.queryForList(orgSql, request.getOrgCode());
        if (orgList.isEmpty()) {
            throw new BusinessException(messageHelper.get("org.not.exist"));
        }

        // 3. 检查用户名是否已存在
        String checkSql = "SELECT 1 FROM sys_user WHERE org_code = ? AND username = ? AND del_flag = '0'";
        List<Map<String, Object>> existList = jdbcTemplate.queryForList(checkSql, request.getOrgCode(), request.getUsername());
        if (!existList.isEmpty()) {
            throw new BusinessException(messageHelper.get("user.username.exists"));
        }

        // 4. 生成用户编码
        String userCode = generateUserCode();

        // 5. 密码编码
        String encodedPassword = encodePassword(request.getPassword());

        // 6. 昵称默认值
        String nickname = request.getNickname();
        if (nickname == null || nickname.isEmpty()) {
            nickname = request.getUsername();
        }

        // 7. 插入用户
        String insertSql = "INSERT INTO sys_user (org_code, user_code, username, password, nickname, status, del_flag) " +
                          "VALUES (?, ?, ?, ?, ?, '0', '0')";
        jdbcTemplate.update(insertSql, request.getOrgCode(), userCode, request.getUsername(), encodedPassword, nickname);

        log.info("用户注册成功: {} ({})", request.getUsername(), request.getOrgCode());

        // 8. 自动登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setOrgCode(request.getOrgCode());
        loginRequest.setUsername(request.getUsername());
        loginRequest.setPassword(request.getPassword());

        return login(loginRequest);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            throw new BusinessException(messageHelper.get("user.not.login"));
        }

        // 1. 查询当前用户密码
        String sql = "SELECT password FROM sys_user WHERE user_code = ? AND org_code = ? AND del_flag = '0'";
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, loginUser.getUserCode(), loginUser.getOrgCode());

        if (userList.isEmpty()) {
            throw new BusinessException(messageHelper.get("user.not.exist"));
        }

        // 2. 验证原密码
        String storedPassword = (String) userList.get(0).get("password");
        if (!validatePassword(request.getOldPassword(), storedPassword)) {
            throw new BusinessException(messageHelper.get("user.old.password.error"));
        }

        // 3. 新密码不能与原密码相同
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(messageHelper.get("user.password.same"));
        }

        // 4. 更新密码
        String newEncodedPassword = encodePassword(request.getNewPassword());
        String updateSql = "UPDATE sys_user SET password = ? WHERE user_code = ? AND org_code = ?";
        jdbcTemplate.update(updateSql, newEncodedPassword, loginUser.getUserCode(), loginUser.getOrgCode());

        log.info("用户修改密码成功: {}", loginUser.getUsername());
    }

    /**
     * 生成用户编码
     */
    private String generateUserCode() {
        return "U" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * 获取组织名称
     */
    private String getOrgName(String orgCode) {
        try {
            String sql = "SELECT org_name FROM sys_org WHERE org_code = ? AND del_flag = '0'";
            return jdbcTemplate.queryForObject(sql, String.class, orgCode);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取用户权限列表
     */
    private Set<String> getUserPermissions(String userCode, String orgCode) {
        String sql = "SELECT DISTINCT m.permission FROM sys_menu m " +
                     "INNER JOIN sys_role_menu rm ON m.menu_code = rm.menu_code " +
                     "INNER JOIN sys_user_role ur ON rm.role_code = ur.role_code AND rm.org_code = ur.org_code " +
                     "WHERE ur.user_code = ? AND ur.org_code = ? AND m.permission IS NOT NULL AND m.permission != ''";
        return Set.copyOf(jdbcTemplate.queryForList(sql, String.class, userCode, orgCode));
    }

    /**
     * 获取用户角色列表
     */
    private Set<String> getUserRoles(String userCode, String orgCode) {
        String sql = "SELECT r.role_key FROM sys_role r " +
                     "INNER JOIN sys_user_role ur ON r.role_code = ur.role_code AND r.org_code = ur.org_code " +
                     "WHERE ur.user_code = ? AND ur.org_code = ?";
        return Set.copyOf(jdbcTemplate.queryForList(sql, String.class, userCode, orgCode));
    }

    /**
     * 获取用户数据权限范围
     */
    private String getUserDataScope(String userCode, String orgCode) {
        String sql = "SELECT MAX(r.data_scope) FROM sys_role r " +
                     "INNER JOIN sys_user_role ur ON r.role_code = ur.role_code AND r.org_code = ur.org_code " +
                     "WHERE ur.user_code = ? AND ur.org_code = ?";
        try {
            String dataScope = jdbcTemplate.queryForObject(sql, String.class, userCode, orgCode);
            return dataScope != null ? dataScope : "3"; // 默认本组织数据
        } catch (Exception e) {
            return "3";
        }
    }

    /**
     * 获取组织及其子组织编码列表
     */
    private List<String> getOrgCodes(String orgCode) {
        String sql = "SELECT org_code FROM sys_org WHERE org_path LIKE " +
                     "(SELECT CONCAT(org_path, '%') FROM sys_org WHERE org_code = ?) AND del_flag = '0'";
        return jdbcTemplate.queryForList(sql, String.class, orgCode);
    }

    /**
     * 获取自定义数据权限组织编码列表
     */
    private List<String> getCustomOrgCodes(String userCode, String orgCode) {
        try {
            String sql = "SELECT DISTINCT rd.org_code FROM sys_role_dept rd " +
                         "INNER JOIN sys_user_role ur ON rd.role_code = ur.role_code AND rd.org_code = ur.org_code " +
                         "WHERE ur.user_code = ? AND ur.org_code = ?";
            return jdbcTemplate.queryForList(sql, String.class, userCode, orgCode);
        } catch (Exception e) {
            // 表不存在时返回空列表
            log.debug("获取自定义数据权限失败，可能表不存在: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 验证密码
     * 数据库存储的是 Base64 编码后的密码
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 数据库中存储的Base64编码密码
     * @return 是否匹配
     */
    private boolean validatePassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        try {
            // 将明文密码进行 Base64 编码
            String encoded = Base64.getEncoder().encodeToString(rawPassword.getBytes());
            return encoded.equals(encodedPassword);
        } catch (Exception e) {
            log.error("密码验证异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 编码密码（用于保存用户时使用）
     *
     * @param rawPassword 明文密码
     * @return Base64编码后的密码
     */
    public String encodePassword(String rawPassword) {
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }

    // ==================== 登录失败次数限制 ====================

    /**
     * 记录登录失败
     */
    private void recordLoginFail(String loginKey) {
        String failKey = CacheConstants.LOGIN_FAIL_COUNT_KEY + loginKey;
        Long failCount = redisTemplate.opsForValue().increment(failKey);
        redisTemplate.expire(failKey, 24, TimeUnit.HOURS);

        if (failCount != null && failCount >= MAX_LOGIN_FAIL_COUNT) {
            // 锁定账户
            String lockKey = CacheConstants.USER_LOCK_KEY + loginKey;
            redisTemplate.opsForValue().set(lockKey, "1", LOGIN_LOCK_TIME, TimeUnit.MINUTES);
            redisTemplate.delete(failKey);

            log.warn("用户登录失败次数过多，已锁定: {} ({}分钟)", loginKey, LOGIN_LOCK_TIME);
            throw new BusinessException(messageHelper.getAsFormat("user.locked.minutes", LOGIN_LOCK_TIME));
        }

        log.debug("登录失败: {} (第{}次)", loginKey, failCount);
    }

    /**
     * 清除登录失败记录
     */
    private void clearLoginFailRecord(String loginKey) {
        String failKey = CacheConstants.LOGIN_FAIL_COUNT_KEY + loginKey;
        redisTemplate.delete(failKey);
    }
}

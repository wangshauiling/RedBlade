package com.redblade.auth.service.impl;

import com.redblade.auth.domain.OnlineUser;
import com.redblade.auth.security.LoginUser;
import com.redblade.auth.service.OnlineUserService;
import com.redblade.common.constant.CacheConstants;
import com.redblade.common.exception.BusinessException;
import com.redblade.common.helper.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 在线用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements OnlineUserService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageHelper messageHelper;

    @Override
    public List<OnlineUser> getOnlineUsers(String orgCode, String username) {
        List<OnlineUser> onlineUsers = new ArrayList<>();

        // 获取所有登录Token
        String pattern = CacheConstants.LOGIN_TOKEN_KEY + "*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            return onlineUsers;
        }

        for (String key : keys) {
            LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(key);
            if (loginUser == null) {
                continue;
            }

            // 过滤条件
            if (orgCode != null && !orgCode.isEmpty() && !orgCode.equals(loginUser.getOrgCode())) {
                continue;
            }
            if (username != null && !username.isEmpty() && !loginUser.getUsername().contains(username)) {
                continue;
            }

            // 获取Token过期时间
            Long expireTime = redisTemplate.getExpire(key, TimeUnit.SECONDS);

            OnlineUser onlineUser = new OnlineUser();
            onlineUser.setUserCode(loginUser.getUserCode());
            onlineUser.setUsername(loginUser.getUsername());
            onlineUser.setNickname(loginUser.getNickname());
            onlineUser.setOrgCode(loginUser.getOrgCode());
            onlineUser.setOrgName(loginUser.getOrgName());
            onlineUser.setDeptCode(loginUser.getDeptCode());
            onlineUser.setToken(loginUser.getAccessToken());
            onlineUser.setLoginTime(LocalDateTime.now().minusSeconds(7200 - expireTime));
            onlineUser.setExpireTime(LocalDateTime.now().plusSeconds(expireTime));

            onlineUsers.add(onlineUser);
        }

        return onlineUsers;
    }

    @Override
    public void kickout(String token) {
        String cacheKey = CacheConstants.LOGIN_TOKEN_KEY + token;
        LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(cacheKey);

        if (loginUser == null) {
            throw new BusinessException(messageHelper.get("user.not.login"));
        }

        redisTemplate.delete(cacheKey);
        log.info("强制下线用户: {} ({})", loginUser.getUsername(), loginUser.getOrgCode());
    }

    @Override
    public void kickoutBatch(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        for (String token : tokens) {
            try {
                kickout(token);
            } catch (Exception e) {
                log.warn("强制下线失败: {}", e.getMessage());
            }
        }
    }

    @Override
    public long getOnlineCount() {
        String pattern = CacheConstants.LOGIN_TOKEN_KEY + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys.size() : 0;
    }
}
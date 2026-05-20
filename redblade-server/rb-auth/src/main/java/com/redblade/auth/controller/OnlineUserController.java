package com.redblade.auth.controller;

import com.redblade.auth.domain.OnlineUser;
import com.redblade.auth.service.OnlineUserService;
import com.redblade.common.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线用户控制器
 */
@Slf4j
@Tag(name = "在线用户管理")
@RestController
@RequestMapping("/online")
@RequiredArgsConstructor
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    /**
     * 获取在线用户列表
     */
    @Operation(summary = "获取在线用户列表")
    @GetMapping("/list")
    public R<List<OnlineUser>> list(@RequestParam(required = false) String orgCode,
                                    @RequestParam(required = false) String username) {
        List<OnlineUser> onlineUsers = onlineUserService.getOnlineUsers(orgCode, username);
        return R.ok(onlineUsers);
    }

    /**
     * 强制下线用户
     */
    @Operation(summary = "强制下线用户")
    @DeleteMapping("/{token}")
    public R<Void> kickout(@PathVariable String token) {
        onlineUserService.kickout(token);
        return R.ok();
    }

    /**
     * 批量强制下线
     */
    @Operation(summary = "批量强制下线")
    @DeleteMapping("/batch")
    public R<Void> kickoutBatch(@RequestBody List<String> tokens) {
        onlineUserService.kickoutBatch(tokens);
        return R.ok();
    }

    /**
     * 获取在线用户数量
     */
    @Operation(summary = "获取在线用户数量")
    @GetMapping("/count")
    public R<Long> count() {
        return R.ok(onlineUserService.getOnlineCount());
    }
}
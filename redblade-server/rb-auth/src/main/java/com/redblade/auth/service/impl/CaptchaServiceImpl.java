package com.redblade.auth.service.impl;

import com.redblade.auth.domain.CaptchaResponse;
import com.redblade.auth.service.CaptchaService;
import com.redblade.common.constant.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 验证码宽度
     */
    private static final int WIDTH = 120;

    /**
     * 验证码高度
     */
    private static final int HEIGHT = 40;

    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 4;

    /**
     * 验证码过期时间（秒）
     */
    private static final long EXPIRE_TIME = 300;

    /**
     * 是否启用验证码（可通过配置文件配置）
     */
    private static final boolean CAPTCHA_ENABLED = true;

    @Override
    public CaptchaResponse generateCaptcha() {
        // 生成验证码Key
        String captchaKey = UUID.randomUUID().toString().replace("-", "");

        // 生成验证码
        String code = generateCode();

        // 存入Redis
        String cacheKey = CacheConstants.CAPTCHA_KEY + captchaKey;
        redisTemplate.opsForValue().set(cacheKey, code, EXPIRE_TIME, TimeUnit.SECONDS);

        // 生成图片
        String captchaImage = generateImage(code);

        return new CaptchaResponse(captchaKey, captchaImage, CAPTCHA_ENABLED);
    }

    @Override
    public boolean validateCaptcha(String captchaKey, String captcha) {
        if (!CAPTCHA_ENABLED) {
            return true;
        }

        if (captchaKey == null || captcha == null) {
            return false;
        }

        String cacheKey = CacheConstants.CAPTCHA_KEY + captchaKey;
        Object storedCode = redisTemplate.opsForValue().get(cacheKey);

        if (storedCode == null) {
            return false;
        }

        // 验证后删除
        redisTemplate.delete(cacheKey);

        // 忽略大小写比较
        return captcha.equalsIgnoreCase(storedCode.toString());
    }

    @Override
    public boolean isCaptchaEnabled() {
        return CAPTCHA_ENABLED;
    }

    /**
     * 生成随机验证码
     */
    private String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * 生成验证码图片（Base64）
     */
    private String generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        Random random = new Random();

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 边框
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        // 干扰线
        for (int i = 0; i < 10; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                       random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }

        // 干扰点
        for (int i = 0; i < 50; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.fillOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
        }

        // 验证码文字
        g.setFont(new Font("Arial", Font.BOLD, 28));
        int x = 10;
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            g.drawString(String.valueOf(code.charAt(i)), x + i * 25, 28);
        }

        g.dispose();

        // 转Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("生成验证码图片失败", e);
            return "";
        }
    }
}
package com.dailybaro.user.auth.service.impl;

import com.dailybaro.user.auth.dto.*;
import com.dailybaro.user.auth.service.AuthService;
import com.dailybaro.user.auth.service.EmailService;
import com.dailybaro.common.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String USER_SERVICE_URL = "http://localhost:8001";
    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final String RESET_TOKEN_PREFIX = "reset:token:";

    @Value("${oauth.qq.app-id:}")
    private String qqAppId;

    @Value("${oauth.qq.app-key:}")
    private String qqAppKey;

    @Value("${oauth.qq.redirect-uri:}")
    private String qqRedirectUri;

    @Override
    public Result<Map<String, Object>> loginWithEmail(EmailLoginDTO emailLoginDTO) {
        try {
            // 从Redis获取验证码
            String cacheKey = EMAIL_CODE_PREFIX + emailLoginDTO.getEmail();
            String cachedCode = (String) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedCode == null || !cachedCode.equals(emailLoginDTO.getVerificationCode())) {
                return Result.fail("验证码错误或已过期");
            }

            // 查询用户
            Map<String, Object> userResult = restTemplate.getForObject(
                USER_SERVICE_URL + "/users/getUserByEmail/" + emailLoginDTO.getEmail(),
                Map.class
            );

            Map<String, Object> userData = null;
            
            // 检查用户是否存在：code为200且data不为null
            if (userResult != null && "200".equals(String.valueOf(userResult.get("code")))) {
                Object dataObj = userResult.get("data");
                if (dataObj != null) {
                    // 用户存在，处理返回的数据
                    if (dataObj instanceof Map) {
                        userData = (Map<String, Object>) dataObj;
                    } else {
                        // 如果是User对象，转换为Map
                        userData = objectMapper.convertValue(dataObj, Map.class);
                    }
                    log.info("找到已存在用户: uid={}, email={}", userData.get("uid"), emailLoginDTO.getEmail());
                }
            }
            
            // 如果用户不存在（data为null或code不是200），自动创建新用户
            if (userData == null) {
                log.info("邮箱用户不存在，自动创建新用户: {}", emailLoginDTO.getEmail());
                
                Map<String, Object> createUserRequest = new HashMap<>();
                // 使用邮箱前缀作为账号
                String emailPrefix = emailLoginDTO.getEmail().split("@")[0];
                createUserRequest.put("account", "email_" + emailPrefix + "_" + System.currentTimeMillis());
                createUserRequest.put("email", emailLoginDTO.getEmail());
                createUserRequest.put("status", 0);
                createUserRequest.put("isdelete", 0);
                createUserRequest.put("energy", 0);
                
                Map<String, Object> createResult = restTemplate.postForObject(
                    USER_SERVICE_URL + "/users/create",
                    createUserRequest,
                    Map.class
                );
                
                if (createResult == null || !"200".equals(String.valueOf(createResult.get("code")))) {
                    log.error("自动创建用户失败: {}", createResult);
                    return Result.fail("自动注册失败，请稍后重试");
                }
                
                Object dataObj = createResult.get("data");
                if (dataObj == null) {
                    log.error("创建用户返回数据为空: {}", createResult);
                    return Result.fail("创建用户失败，数据异常");
                }
                
                // 处理返回的数据，可能是Map或User对象
                if (dataObj instanceof Map) {
                    userData = (Map<String, Object>) dataObj;
                } else {
                    // 如果是User对象，转换为Map
                    userData = objectMapper.convertValue(dataObj, Map.class);
                }
                log.info("自动创建用户成功: uid={}, email={}", userData.get("uid"), emailLoginDTO.getEmail());
            }

            // 删除验证码
            redisTemplate.delete(cacheKey);

            // 返回用户信息
            Map<String, Object> result = new HashMap<>();
            result.put("uid", userData.get("uid"));
            result.put("account", userData.get("account"));
            result.put("email", userData.get("email"));
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("邮箱登录失败", e);
            return Result.fail("登录失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> sendEmailVerification(EmailVerificationDTO emailVerificationDTO) {
        try {
            String code = emailService.generateVerificationCode();
            String cacheKey = EMAIL_CODE_PREFIX + emailVerificationDTO.getEmail();
            
            // 存储验证码到Redis，5分钟过期
            redisTemplate.opsForValue().set(cacheKey, code, 5, TimeUnit.MINUTES);
            log.info("验证码已存储到Redis，邮箱: {}, 验证码: {}", emailVerificationDTO.getEmail(), code);
            
            // 检查是否在模拟模式
            boolean isMockMode = emailService.isMockMode();
            
            // 在模拟模式下，快速返回，不发送真实邮件
            if (isMockMode) {
                String message = "验证码已生成（开发模式）: " + code + "，请查看服务器日志或使用此验证码登录";
                log.info("开发模式：验证码已返回给前端，邮箱: {}, 验证码: {}", emailVerificationDTO.getEmail(), code);
                return Result.success(message);
            }
            
            // 尝试发送邮件（在真实邮件模式下）
            // 注意：即使邮件发送失败，验证码也已经存储到Redis，用户仍然可以使用
            try {
                emailService.sendVerificationEmail(
                    emailVerificationDTO.getEmail(),
                    code,
                    emailVerificationDTO.getType() != null ? emailVerificationDTO.getType() : "login"
                );
                log.info("验证码发送完成，邮箱: {}", emailVerificationDTO.getEmail());
                return Result.success("验证码已发送到邮箱");
            } catch (Exception emailException) {
                // 验证码已经保存到Redis，检查是否真的保存成功
                String savedCode = (String) redisTemplate.opsForValue().get(cacheKey);
                if (savedCode != null && savedCode.equals(code)) {
                    log.warn("邮件发送失败，但验证码已成功保存到Redis，邮箱: {}, 验证码: {}，错误: {}", 
                        emailVerificationDTO.getEmail(), code, emailException.getMessage());
                    // 验证码已保存，返回成功（开发环境可以返回验证码，生产环境不返回）
                    if (isMockMode) {
                        return Result.success("验证码已生成: " + code + "（邮件发送失败，请使用此验证码登录）");
                    } else {
                        return Result.success("验证码已生成，请查看邮箱（如未收到，请查看服务器日志或联系管理员）");
                    }
                } else {
                    log.error("邮件发送失败，且验证码未正确保存到Redis，邮箱: {}, 错误: {}", 
                        emailVerificationDTO.getEmail(), emailException.getMessage());
                    return Result.fail("验证码生成失败，请稍后重试");
                }
            }
            
        } catch (Exception e) {
            log.error("发送验证码失败，异常: {}", e.getMessage(), e);
            return Result.fail("发送验证码失败，请稍后重试");
        }
    }

    @Override
    public Result<Map<String, Object>> loginWithWechat(WechatLoginDTO wechatLoginDTO) {
        return Result.fail("WECHAT_LOGIN_NOT_IMPLEMENTED");
    }

    @Override
    public Result<Map<String, Object>> loginWithQQ(QQLoginDTO qqLoginDTO) {
        try {
            // 先通过 code 获取 access_token
            String tokenUrl = String.format(
                "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
                qqAppId, qqAppKey, qqLoginDTO.getCode(), qqRedirectUri
            );
            
            String tokenResponse = restTemplate.getForObject(tokenUrl, String.class);
            if (tokenResponse == null || tokenResponse.contains("error")) {
                return Result.fail("QQ登录失败：无法获取access_token");
            }
            
            // 解析 access_token
            String accessToken = extractAccessTokenFromResponse(tokenResponse);
            if (accessToken == null) {
                return Result.fail("QQ登录失败：无法解析access_token");
            }
            
            // 调用QQ API获取用户信息
            String url = String.format(
                "https://graph.qq.com/oauth2.0/me?access_token=%s",
                accessToken
            );
            
            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.contains("error")) {
                return Result.fail("QQ登录失败：无效的access_token");
            }

            // 解析openid
            String openid = extractOpenidFromResponse(response);
            if (openid == null) {
                return Result.fail("QQ登录失败：无法获取用户信息");
            }

            // 查询或创建用户
            Map<String, Object> userResult = restTemplate.getForObject(
                USER_SERVICE_URL + "/users/getUserByQQOpenid/" + openid,
                Map.class
            );

            Map<String, Object> userData;
            if (userResult == null || !"200".equals(String.valueOf(userResult.get("code")))) {
                // 用户不存在，创建新用户
                Map<String, Object> createUserRequest = new HashMap<>();
                createUserRequest.put("account", "qq_" + openid);
                createUserRequest.put("qqOpenid", openid);
                createUserRequest.put("status", 0);
                createUserRequest.put("isdelete", 0);
                
                Map<String, Object> createResult = restTemplate.postForObject(
                    USER_SERVICE_URL + "/users/create",
                    createUserRequest,
                    Map.class
                );
                
                if (createResult == null || !"200".equals(String.valueOf(createResult.get("code")))) {
                    return Result.fail("创建用户失败");
                }
                userData = (Map<String, Object>) createResult.get("data");
            } else {
                userData = (Map<String, Object>) userResult.get("data");
            }

            // 返回用户信息
            Map<String, Object> result = new HashMap<>();
            result.put("uid", userData.get("uid"));
            result.put("account", userData.get("account"));
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("QQ登录失败", e);
            return Result.fail("QQ登录失败：" + e.getMessage());
        }
    }

    private String extractAccessTokenFromResponse(String response) {
        try {
            // 响应格式: access_token=xxx&expires_in=xxx&refresh_token=xxx
            if (response.contains("access_token=")) {
                int start = response.indexOf("access_token=") + 13;
                int end = response.indexOf("&", start);
                if (end == -1) {
                    end = response.length();
                }
                if (start > 12 && end > start) {
                    return response.substring(start, end);
                }
            }
        } catch (Exception e) {
            log.error("解析access_token失败", e);
        }
        return null;
    }

    private String extractOpenidFromResponse(String response) {
        try {
            // 响应格式: callback({"client_id":"xxx","openid":"xxx"});
            int start = response.indexOf("\"openid\":\"") + 10;
            int end = response.indexOf("\"", start);
            if (start > 9 && end > start) {
                return response.substring(start, end);
            }
        } catch (Exception e) {
            log.error("解析openid失败", e);
        }
        return null;
    }

    @Override
    public Result<Map<String, Object>> loginWithWeibo(WeiboLoginDTO weiboLoginDTO) {
        return Result.fail("WEIBO_LOGIN_NOT_IMPLEMENTED");
    }

    @Override
    public Result<Map<String, Object>> refreshToken(String refreshToken) {
        return Result.fail("REFRESH_TOKEN_NOT_IMPLEMENTED");
    }

    @Override
    public Result<String> logout(String token) {
        return Result.success("OK");
    }

    @Override
    public Result<Map<String, Object>> verifyToken(String token) {
        Map<String, Object> data = new HashMap<>();
        data.put("valid", false);
        return Result.success(data);
    }

    @Override
    public Result<Map<String, String>> getOAuthUrl(String provider) {
        Map<String, String> data = new HashMap<>();
        if ("qq".equals(provider)) {
            // 验证QQ配置是否有效
            if (qqAppId == null || qqAppId.isEmpty() || 
                qqAppId.equals("your-qq-app-id") || 
                qqAppId.startsWith("${")) {
                log.warn("QQ登录配置未设置，app-id: {}", qqAppId);
                return Result.fail("QQ登录功能暂未配置，请联系管理员或使用其他登录方式");
            }
            
            if (qqRedirectUri == null || qqRedirectUri.isEmpty() || 
                qqRedirectUri.equals("http://localhost:3000/auth/qq/callback") ||
                qqRedirectUri.startsWith("${")) {
                log.warn("QQ登录回调地址未配置，redirect-uri: {}", qqRedirectUri);
                return Result.fail("QQ登录回调地址未配置，请联系管理员");
            }
            
            String state = UUID.randomUUID().toString();
            String url = String.format(
                "https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s&scope=get_user_info",
                qqAppId,
                qqRedirectUri,
                state
            );
            data.put("url", url);
            data.put("state", state);
        } else {
            data.put("url", "");
        }
        data.put("provider", provider);
        return Result.success(data);
    }

    @Override
    public Result<Map<String, Object>> oauthCallback(String provider, String code, String state) {
        return Result.fail("OAUTH_CALLBACK_NOT_IMPLEMENTED");
    }

    @Override
    public Result<Map<String, Object>> getUserInfo(String token) {
        return Result.fail("GET_USER_INFO_NOT_IMPLEMENTED");
    }

    @Override
    public Result<String> changePassword(ChangePasswordDTO changePasswordDTO, String token) {
        return Result.fail("CHANGE_PASSWORD_NOT_IMPLEMENTED");
    }

    @Override
    public Result<String> forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        try {
            // 查询用户
            Map<String, Object> userResult = restTemplate.getForObject(
                USER_SERVICE_URL + "/users/getUserByEmail/" + forgotPasswordDTO.getEmail(),
                Map.class
            );

            if (userResult == null || !"200".equals(String.valueOf(userResult.get("code")))) {
                // 即使用户不存在，也返回成功，防止邮箱枚举
                return Result.success("如果该邮箱已注册，重置密码邮件已发送");
            }

            // 生成重置token
            String resetToken = UUID.randomUUID().toString();
            String cacheKey = RESET_TOKEN_PREFIX + resetToken;
            
            // 存储token到Redis，1小时过期
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("email", forgotPasswordDTO.getEmail());
            redisTemplate.opsForValue().set(cacheKey, tokenData, 1, TimeUnit.HOURS);
            
            // 发送重置密码邮件
            emailService.sendPasswordResetEmail(forgotPasswordDTO.getEmail(), resetToken);
            
            return Result.success("重置密码邮件已发送");
        } catch (Exception e) {
            log.error("忘记密码处理失败", e);
            // 即使出错也返回成功，防止信息泄露
            return Result.success("如果该邮箱已注册，重置密码邮件已发送");
        }
    }

    @Override
    public Result<String> resetPassword(ResetPasswordDTO resetPasswordDTO) {
        try {
            String cacheKey = RESET_TOKEN_PREFIX + resetPasswordDTO.getToken();
            Map<String, Object> tokenData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            
            if (tokenData == null) {
                return Result.fail("重置链接无效或已过期");
            }

            String email = (String) tokenData.get("email");
            
            // 查询用户
            Map<String, Object> userResult = restTemplate.getForObject(
                USER_SERVICE_URL + "/users/getUserByEmail/" + email,
                Map.class
            );

            if (userResult == null || !"200".equals(String.valueOf(userResult.get("code")))) {
                return Result.fail("用户不存在");
            }

            Map<String, Object> userData = (Map<String, Object>) userResult.get("data");
            Long uid = Long.valueOf(String.valueOf(userData.get("uid")));
            
            // 更新密码
            String encodedPassword = passwordEncoder.encode(resetPasswordDTO.getNewPassword());
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("uid", uid);
            updateRequest.put("password", encodedPassword);
            
            restTemplate.postForObject(
                USER_SERVICE_URL + "/users/updatePassword",
                updateRequest,
                Map.class
            );
            
            // 删除token
            redisTemplate.delete(cacheKey);
            
            return Result.success("密码重置成功");
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.fail("重置密码失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> bindThirdParty(String provider, BindThirdPartyDTO bindDTO, String token) {
        return Result.fail("BIND_THIRD_PARTY_NOT_IMPLEMENTED");
    }

    @Override
    public Result<String> unbindThirdParty(String provider, String token) {
        return Result.fail("UNBIND_THIRD_PARTY_NOT_IMPLEMENTED");
    }

    @Override
    public Result<Map<String, Boolean>> getBindStatus(String token) {
        Map<String, Boolean> data = new HashMap<>();
        return Result.success(data);
    }
}

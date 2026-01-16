package com.dailybaro.user.auth.controller;

import com.dailybaro.user.auth.dto.*;
import com.dailybaro.user.auth.service.AuthService;
import com.dailybaro.common.util.Result;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 邮箱验证码登录
     */
    @PostMapping("/login/email")
    @CircuitBreaker(name = "email-service")
    public Result<Map<String, Object>> loginWithEmail(@Valid @RequestBody EmailLoginDTO emailLoginDTO) {
        log.info("邮箱登录请求: {}", emailLoginDTO.getEmail());
        return authService.loginWithEmail(emailLoginDTO);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/email/send-verification")
    @CircuitBreaker(name = "email-service")
    public Result<String> sendEmailVerification(@Valid @RequestBody EmailVerificationDTO emailVerificationDTO) {
        log.info("发送邮箱验证码: {}", emailVerificationDTO.getEmail());
        return authService.sendEmailVerification(emailVerificationDTO);
    }

    /**
     * 微信登录
     */
    @PostMapping("/login/wechat")
    public Result<Map<String, Object>> loginWithWechat(@RequestBody Map<String, Object> body) {
        Object codeObj = body != null ? body.get("code") : null;
        String code = codeObj != null ? String.valueOf(codeObj) : null;
        if (code == null || code.trim().isEmpty()) {
            log.warn("微信登录请求参数不完整: {}", body);
            return Result.fail("WECHAT_LOGIN_CODE_MISSING");
        }
        log.info("微信登录请求: {}", code);
        WechatLoginDTO dto = new WechatLoginDTO();
        dto.setCode(code);
        return authService.loginWithWechat(dto);
    }

    /**
     * QQ登录
     */
    @PostMapping("/login/qq")
    public Result<Map<String, Object>> loginWithQQ(@Valid @RequestBody QQLoginDTO qqLoginDTO) {
        log.info("QQ登录请求: {}", qqLoginDTO.getCode());
        return authService.loginWithQQ(qqLoginDTO);
    }

    /**
     * 微博登录
     */
    @PostMapping("/login/weibo")
    public Result<Map<String, Object>> loginWithWeibo(@Valid @RequestBody WeiboLoginDTO weiboLoginDTO) {
        log.info("微博登录请求: {}", weiboLoginDTO.getCode());
        return authService.loginWithWeibo(weiboLoginDTO);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        log.info("刷新Token请求");
        return authService.refreshToken(refreshToken);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = extractToken(request);
        log.info("用户登出请求");
        return authService.logout(token);
    }

    /**
     * 验证Token
     */
    @GetMapping("/verify")
    public Result<Map<String, Object>> verifyToken(HttpServletRequest request) {
        String token = extractToken(request);
        log.info("验证Token请求");
        return authService.verifyToken(token);
    }

    /**
     * 获取第三方登录授权URL
     */
    @GetMapping("/oauth/url")
    public Result<Map<String, String>> getOAuthUrl(@RequestParam String provider) {
        log.info("获取OAuth授权URL: {}", provider);
        return authService.getOAuthUrl(provider);
    }

    /**
     * 第三方登录回调
     */
    @GetMapping("/oauth/callback")
    public Result<Map<String, Object>> oauthCallback(@RequestParam String provider, 
                                                    @RequestParam String code,
                                                    @RequestParam(required = false) String state) {
        log.info("OAuth回调: provider={}, code={}", provider, code);
        return authService.oauthCallback(provider, code, state);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user/info")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        String token = extractToken(request);
        log.info("获取用户信息请求");
        return authService.getUserInfo(token);
    }

    /**
     * 修改密码
     */
    @PostMapping("/password/change")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO,
                                        HttpServletRequest request) {
        String token = extractToken(request);
        log.info("修改密码请求");
        return authService.changePassword(changePasswordDTO, token);
    }

    /**
     * 忘记密码
     */
    @PostMapping("/password/forgot")
    @CircuitBreaker(name = "email-service")
    public Result<String> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        log.info("忘记密码请求: {}", forgotPasswordDTO.getEmail());
        return authService.forgotPassword(forgotPasswordDTO);
    }

    /**
     * 重置密码
     */
    @PostMapping("/password/reset")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        log.info("重置密码请求");
        return authService.resetPassword(resetPasswordDTO);
    }

    /**
     * 绑定第三方账号
     */
    @PostMapping("/bind/{provider}")
    public Result<String> bindThirdParty(@PathVariable String provider,
                                        @Valid @RequestBody BindThirdPartyDTO bindDTO,
                                        HttpServletRequest request) {
        String token = extractToken(request);
        log.info("绑定第三方账号: provider={}", provider);
        return authService.bindThirdParty(provider, bindDTO, token);
    }

    /**
     * 解绑第三方账号
     */
    @PostMapping("/unbind/{provider}")
    public Result<String> unbindThirdParty(@PathVariable String provider,
                                          HttpServletRequest request) {
        String token = extractToken(request);
        log.info("解绑第三方账号: provider={}", provider);
        return authService.unbindThirdParty(provider, token);
    }

    /**
     * 获取绑定状态
     */
    @GetMapping("/bind/status")
    public Result<Map<String, Boolean>> getBindStatus(HttpServletRequest request) {
        String token = extractToken(request);
        log.info("获取绑定状态请求");
        return authService.getBindStatus(token);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

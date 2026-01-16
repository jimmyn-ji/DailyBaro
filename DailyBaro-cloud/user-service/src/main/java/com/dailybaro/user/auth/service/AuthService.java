package com.dailybaro.user.auth.service;

import com.dailybaro.user.auth.dto.*;
import com.dailybaro.common.util.Result;

import java.util.Map;

public interface AuthService {
    
    /**
     * 邮箱验证码登录
     */
    Result<Map<String, Object>> loginWithEmail(EmailLoginDTO emailLoginDTO);
    
    /**
     * 发送邮箱验证码
     */
    Result<String> sendEmailVerification(EmailVerificationDTO emailVerificationDTO);
    
    /**
     * 微信登录
     */
    Result<Map<String, Object>> loginWithWechat(WechatLoginDTO wechatLoginDTO);
    
    /**
     * QQ登录
     */
    Result<Map<String, Object>> loginWithQQ(QQLoginDTO qqLoginDTO);
    
    /**
     * 微博登录
     */
    Result<Map<String, Object>> loginWithWeibo(WeiboLoginDTO weiboLoginDTO);
    
    /**
     * 刷新Token
     */
    Result<Map<String, Object>> refreshToken(String refreshToken);
    
    /**
     * 登出
     */
    Result<String> logout(String token);
    
    /**
     * 验证Token
     */
    Result<Map<String, Object>> verifyToken(String token);
    
    /**
     * 获取OAuth授权URL
     */
    Result<Map<String, String>> getOAuthUrl(String provider);
    
    /**
     * OAuth回调处理
     */
    Result<Map<String, Object>> oauthCallback(String provider, String code, String state);
    
    /**
     * 获取用户信息
     */
    Result<Map<String, Object>> getUserInfo(String token);
    
    /**
     * 修改密码
     */
    Result<String> changePassword(ChangePasswordDTO changePasswordDTO, String token);
    
    /**
     * 忘记密码
     */
    Result<String> forgotPassword(ForgotPasswordDTO forgotPasswordDTO);
    
    /**
     * 重置密码
     */
    Result<String> resetPassword(ResetPasswordDTO resetPasswordDTO);
    
    /**
     * 绑定第三方账号
     */
    Result<String> bindThirdParty(String provider, BindThirdPartyDTO bindDTO, String token);
    
    /**
     * 解绑第三方账号
     */
    Result<String> unbindThirdParty(String provider, String token);
    
    /**
     * 获取绑定状态
     */
    Result<Map<String, Boolean>> getBindStatus(String token);
}

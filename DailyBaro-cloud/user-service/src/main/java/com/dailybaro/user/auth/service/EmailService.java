package com.dailybaro.user.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${verification.email.code-length:6}")
    private int codeLength;
    
    @Value("${email.mock.enabled:true}")
    private boolean mockEnabled;
    
    /**
     * 检查是否在模拟模式
     */
    public boolean isMockMode() {
        return mockEnabled || isEmailNotConfigured();
    }

    /**
     * 生成验证码
     */
    public String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送验证码邮件
     */
    public void sendVerificationEmail(String toEmail, String code, String type) {
        // 检查是否启用模拟模式（开发环境）
        if (isMockMode()) {
            log.warn("========================================");
            log.warn("邮件服务运行在模拟模式（开发环境）");
            log.warn("收件人邮箱: {}", toEmail);
            log.warn("验证码类型: {}", type);
            log.warn("验证码: {}", code);
            log.warn("========================================");
            log.info("提示：在生产环境中，请配置 EMAIL_USERNAME 和 EMAIL_PASSWORD 环境变量以启用真实邮件发送");
            return;
        }
        
        try {
            // 检查邮件服务是否可用
            if (mailSender == null) {
                log.error("邮件服务未初始化，mailSender 为 null，切换到模拟模式");
                log.warn("验证码: {}", code);
                return;
            }
            
            log.info("准备发送验证码邮件，收件人: {}, 发件人: {}", toEmail, fromEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            
            String subject = getEmailSubject(type);
            String content = getEmailContent(code, type);
            
            message.setSubject(subject);
            message.setText(content);
            
            log.debug("邮件内容 - 主题: {}, 收件人: {}", subject, toEmail);
            
            mailSender.send(message);
            log.info("验证码邮件发送成功: {}", toEmail);
        } catch (org.springframework.mail.MailException e) {
            log.error("邮件发送异常 (MailException): {}，验证码已保存到Redis", e.getMessage());
            log.warn("验证码: {}（用户仍可使用此验证码登录）", code);
            // 抛出异常，让调用方捕获并处理
            // 调用方会检查Redis中是否已保存验证码，如果已保存则返回成功
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("验证码邮件发送失败: {}, 异常类型: {}，验证码已保存到Redis", 
                e.getMessage(), e.getClass().getName());
            log.warn("验证码: {}（用户仍可使用此验证码登录）", code);
            // 抛出异常，让调用方捕获并处理
            // 调用方会检查Redis中是否已保存验证码，如果已保存则返回成功
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查邮件是否未配置
     */
    private boolean isEmailNotConfigured() {
        return fromEmail == null || 
               fromEmail.isEmpty() || 
               fromEmail.equals("your-email@qq.com") ||
               fromEmail.startsWith("${");
    }

    /**
     * 获取邮件主题
     */
    private String getEmailSubject(String type) {
        switch (type) {
            case "login":
                return "DailyBaro - 登录验证码";
            case "register":
                return "DailyBaro - 注册验证码";
            case "reset":
                return "DailyBaro - 重置密码验证码";
            default:
                return "DailyBaro - 验证码";
        }
    }

    /**
     * 获取邮件内容
     */
    private String getEmailContent(String code, String type) {
        String action = "";
        switch (type) {
            case "login":
                action = "登录";
                break;
            case "register":
                action = "注册";
                break;
            case "reset":
                action = "重置密码";
                break;
            default:
                action = "验证";
        }

        return String.format(
            "您好！\n\n" +
            "您的DailyBaro %s验证码是：%s\n\n" +
            "验证码有效期为5分钟，请尽快使用。\n" +
            "如果这不是您的操作，请忽略此邮件。\n\n" +
            "祝您使用愉快！\n" +
            "DailyBaro团队",
            action, code
        );
    }

    /**
     * 发送密码重置邮件
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        // 检查是否启用模拟模式（开发环境）
        if (isMockMode()) {
            log.warn("========================================");
            log.warn("邮件服务运行在模拟模式（开发环境）");
            log.warn("密码重置邮件 - 收件人: {}", toEmail);
            log.warn("重置令牌: {}", resetToken);
            log.warn("重置链接: http://localhost:3000/reset-password?token={}", resetToken);
            log.warn("========================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("DailyBaro - 密码重置");
            
            String content = String.format(
                "您好！\n\n" +
                "您请求重置DailyBaro账户密码。\n" +
                "请点击以下链接重置密码：\n\n" +
                "http://localhost:3000/reset-password?token=%s\n\n" +
                "此链接有效期为1小时。\n" +
                "如果这不是您的操作，请忽略此邮件。\n\n" +
                "祝您使用愉快！\n" +
                "DailyBaro团队",
                resetToken
            );
            
            message.setText(content);
            mailSender.send(message);
            log.info("密码重置邮件发送成功: {}", toEmail);
        } catch (Exception e) {
            log.error("密码重置邮件发送失败: {}，切换到模拟模式", e.getMessage());
            log.warn("重置令牌: {}", resetToken);
            if (mockEnabled) {
                return;
            }
            throw new RuntimeException("邮件发送失败");
        }
    }
}

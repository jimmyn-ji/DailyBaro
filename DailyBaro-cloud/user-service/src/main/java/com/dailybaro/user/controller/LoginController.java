package com.dailybaro.user.controller;

import com.dailybaro.user.mapper.UserMapper;
import com.dailybaro.user.model.User;
import com.dailybaro.user.model.dto.UserPwDTO;
import com.dailybaro.user.model.dto.UserRegisterDTO;
import com.dailybaro.user.model.vo.UserInfoVO;
import com.dailybaro.common.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${wx.appId}")
    private String appId;

    @Value("${wx.appSecret}")
    private String appSecret;

    @PostMapping("/doLogin")
    public Result<UserInfoVO> doLogin(@RequestBody UserPwDTO userPwDTO) {
        User user = userMapper.selectByAccount(userPwDTO.getAccount());

        if (user == null) {
            return Result.fail("账号不存在");
        }
        
        // 支持两种情况：
        // 1) 数据库已是BCrypt密文 → 使用matches校验
        // 2) 早期数据可能为明文 → 直接比较作为回退
        boolean passwordOk = false;
        try {
            passwordOk = passwordEncoder.matches(userPwDTO.getPassword(), user.getPassword());
        } catch (Exception ignored) {}
        if (!passwordOk) {
            passwordOk = user.getPassword() != null && user.getPassword().equals(userPwDTO.getPassword());
        }
        if (!passwordOk) {
            return Result.fail("密码错误");
        }
        
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUid(user.getUid());
        userInfo.setAccount(user.getAccount());
        
        // 每日登录奖励 +5 能量（每天只能获得一次）
        try {
            String today = java.time.LocalDate.now().toString();
            Integer rewardCount = userMapper.checkDailyLoginReward(user.getUid(), today);
            
            if (rewardCount == null || rewardCount == 0) {
                // 今天还没有获得奖励，可以给予奖励
                userMapper.increaseEnergy(user.getUid(), 5);
                userMapper.recordDailyLoginReward(user.getUid(), today, 5);
                userInfo.setEnergyReward(5);
            } else {
                // 今天已经获得过奖励了
                userInfo.setEnergyReward(0);
            }
        } catch (Exception e) {
            // 如果奖励失败，不影响登录
            userInfo.setEnergyReward(0);
        }
        
        return Result.success(userInfo);
    }

    @PostMapping("/doRegister")
    public Result<UserInfoVO> doRegister(@RequestBody UserRegisterDTO userRegisterDTO) {
        // 验证密码确认
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getConfirmPassword())) {
            return Result.fail("两次输入的密码不一致");
        }

        // 检查账号是否已存在
        User existingUser = userMapper.selectByAccount(userRegisterDTO.getAccount());
        if (existingUser != null) {
            return Result.fail("账号已存在");
        }

        // 创建新用户
        User newUser = new User();
        newUser.setAccount(userRegisterDTO.getAccount());
        newUser.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
        newUser.setPhone(userRegisterDTO.getPhone());
        newUser.setEmail(userRegisterDTO.getEmail());
        newUser.setStatus(0); // 正常状态
        newUser.setIsdelete(0); // 未删除
        newUser.setEnergy(0); // 初始能量

        try {
            userMapper.insertUser(newUser);
            
            // 返回用户信息
            UserInfoVO userInfo = new UserInfoVO();
            userInfo.setUid(newUser.getUid());
            userInfo.setAccount(newUser.getAccount());
            userInfo.setEnergyReward(0);
            
            return Result.success(userInfo);
        } catch (Exception e) {
            return Result.fail("注册失败：" + e.getMessage());
        }
    }

    /**
     * 微信小程序一键登录：
     * 前端提供 code（wx.login 获取），后端调用微信 jscode2session 获取 openid，
     * 然后根据 openid 创建或查找用户并返回基础用户信息。
     */
    @PostMapping("/wxLogin")
    public Result<UserInfoVO> wxLogin(@RequestBody Map<String, Object> body) {
        System.out.println("user-service 接收到的微信登录请求 body: " + body);
        if (body != null) {
            System.out.println("user-service body 中的所有键: " + body.keySet());
            System.out.println("user-service body 中的 code: " + body.get("code"));
        }
        
        Object codeObj = body != null ? body.get("code") : null;
        String code = codeObj != null ? String.valueOf(codeObj) : null;
        if (code == null || code.trim().isEmpty() || "null".equals(code)) {
            System.out.println("user-service 检测到 code 为空，body 内容: " + body);
            return Result.fail("微信登录失败：缺少授权码");
        }

        try {
            // 检查配置
            if (appId == null || appId.isEmpty() || appSecret == null || appSecret.isEmpty()) {
                System.out.println("微信配置错误 - appId: " + (appId != null ? appId.substring(0, Math.min(10, appId.length())) + "..." : "null") + 
                                 ", appSecret: " + (appSecret != null ? "已配置" : "未配置"));
                return Result.fail("微信登录失败：服务器配置错误，请联系管理员");
            }
            
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appId, appSecret, code
            );

            System.out.println("调用微信API，URL: " + url.replace(appSecret, "***"));
            System.out.println("使用的 code: " + code);

            RestTemplate restTemplate = new RestTemplate();
            String responseBody = restTemplate.getForObject(url, String.class);
            
            System.out.println("微信API原始响应: " + responseBody);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> sessionObj = mapper.readValue(responseBody, Map.class);

            Object openidObj = sessionObj.get("openid");
            if (openidObj == null) {
                String errcode = String.valueOf(sessionObj.get("errcode"));
                String errMsg = String.valueOf(sessionObj.get("errmsg"));
                System.out.println("微信API返回错误 - errcode: " + errcode + ", errmsg: " + errMsg);
                
                // 根据错误码提供更友好的错误信息
                String friendlyMsg = errMsg;
                if ("40029".equals(errcode)) {
                    friendlyMsg = "授权码无效或已过期，请重新登录";
                } else if ("40163".equals(errcode)) {
                    friendlyMsg = "授权码已被使用，请重新获取";
                } else if ("40013".equals(errcode)) {
                    friendlyMsg = "AppID无效，请联系管理员";
                } else if ("40125".equals(errcode)) {
                    friendlyMsg = "AppSecret无效，请联系管理员";
                }
                
                return Result.fail("微信登录失败：" + friendlyMsg + " (错误码: " + errcode + ")");
            }

            String openid = String.valueOf(openidObj);
            System.out.println("成功获取 openid: " + openid);

            // 查询或创建用户（仅绑定 openid，手机号等信息可以后续完善）
            User user = userMapper.selectByWxOpenid(openid);
            if (user == null) {
                user = new User();
                user.setWxOpenid(openid);
                user.setStatus(0);
                user.setIsdelete(0);
                // 使用 openid 作为占位账号，后续可让用户自行绑定手机号/账号
                user.setAccount("wx_" + openid);
                
                // 为微信一键登录用户生成一个随机占位密码，避免数据库 NOT NULL 约束报错
                // 这个密码不会在前端使用，仅用于满足表结构要求
                try {
                    String rawRandomPwd = java.util.UUID.randomUUID().toString();
                    String encodedPwd = passwordEncoder.encode(rawRandomPwd);
                    
                    // 验证密码编码是否成功
                    if (encodedPwd == null || encodedPwd.isEmpty()) {
                        System.out.println("密码编码失败，使用默认密码");
                        encodedPwd = passwordEncoder.encode("wx_default_password_" + openid);
                    }
                    
                    user.setPassword(encodedPwd);
                    System.out.println("设置用户密码成功，密码长度: " + (encodedPwd != null ? encodedPwd.length() : 0));
                } catch (Exception e) {
                    System.out.println("密码编码异常: " + e.getMessage());
                    e.printStackTrace();
                    // 如果编码失败，使用一个固定的默认密码
                    user.setPassword(passwordEncoder.encode("wx_default_password_" + openid));
                }
                
                // 验证所有必填字段都已设置
                System.out.println("创建用户前验证 - account: " + user.getAccount() + 
                                 ", password: " + (user.getPassword() != null ? "已设置(" + user.getPassword().length() + ")" : "未设置") +
                                 ", wxOpenid: " + user.getWxOpenid() +
                                 ", status: " + user.getStatus() +
                                 ", isdelete: " + user.getIsdelete());
                
                int insertResult = userMapper.insertUser(user);
                System.out.println("插入用户结果: " + insertResult + ", 新用户ID: " + user.getUid());
            }

            UserInfoVO userInfo = new UserInfoVO();
            userInfo.setUid(user.getUid());
            userInfo.setAccount(user.getAccount());
            userInfo.setEnergyReward(0);

            return Result.success(userInfo);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.out.println("JSON解析失败: " + e.getMessage());
            e.printStackTrace();
            return Result.fail("微信登录失败：服务器响应解析错误，请稍后重试");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.out.println("HTTP请求失败: " + e.getStatusCode() + " - " + e.getMessage());
            e.printStackTrace();
            return Result.fail("微信登录失败：无法连接到微信服务器，请检查网络");
        } catch (Exception e) {
            System.out.println("微信登录异常: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return Result.fail("微信登录失败：" + e.getMessage());
        }
    }
} 
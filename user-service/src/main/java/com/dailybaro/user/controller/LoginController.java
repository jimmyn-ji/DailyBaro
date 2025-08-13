package com.dailybaro.user.controller;

import com.dailybaro.user.mapper.UserMapper;
import com.dailybaro.user.model.User;
import com.dailybaro.user.model.dto.UserPwDTO;
import com.dailybaro.user.model.dto.UserRegisterDTO;
import com.dailybaro.user.model.vo.UserInfoVO;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
} 
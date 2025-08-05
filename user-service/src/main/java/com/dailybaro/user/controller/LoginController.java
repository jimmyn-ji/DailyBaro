package com.dailybaro.user.controller;

import com.dailybaro.user.mapper.UserMapper;
import com.dailybaro.user.model.User;
import com.dailybaro.user.model.dto.UserPwDTO;
import com.dailybaro.user.model.vo.UserInfoVO;
import com.dailybaro.user.util.Result;
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

        UserInfoVO userInfo = new UserInfoVO();

        if (user == null) {
            return Result.fail("账号不存在");
        }
        if (user != null && passwordEncoder.matches(userPwDTO.getPassword(), user.getPassword())) {
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
        }
        return Result.success(userInfo);
    }
} 
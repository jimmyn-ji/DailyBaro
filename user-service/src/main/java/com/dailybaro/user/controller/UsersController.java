package com.dailybaro.user.controller;

import com.github.pagehelper.PageInfo;
import com.dailybaro.user.model.User;
import com.dailybaro.user.model.dto.UpdatePwdDTO;
import com.dailybaro.user.service.UserService;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     * @param pageNum 页码，默认为1
     * @param pageSize 每页大小，默认为10
     * @return 分页用户列表
     */
    @GetMapping("/getUsersByPage")
    public Result<PageInfo<User>> getUsersByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<User> pageInfo = userService.getUsersByPage(pageNum, pageSize);
        return Result.success(pageInfo);
    }

    @GetMapping("/getMyInfo/{uid}")
    public Result<User> getMyInfo(@PathVariable Long uid) {
        User user = userService.getMyInfo(uid);
        return Result.success(user);
    }

    /**
     * 根据账号查询用户信息
     * @param account 账号
     * @return 用户信息
     */
    @GetMapping("/getUserByAccount/{account}")
    public Result<User> getUserByAccount(@PathVariable String account) {
        User user = userService.getUserByAccount(account);
        return Result.success(user);
    }

    /**
     * 修改密码
     * @return 操作结果
     */
    @RequestMapping("/changePassword/{uid}")
    public Result<User> changePassword(@RequestBody UpdatePwdDTO updatePwdDTO) {
        userService.changePassword(updatePwdDTO);
        return Result.success();
    }

    /**
     * 修改个人信息
     * @param user 用户信息
     * @return 操作结果
     */
    @PostMapping("/updateUserInfo")
    public Result<String> updateUserInfo(@RequestBody User user) {
        userService.updateUserInfo(user);
        return Result.success("个人信息修改成功");
    }

    @PostMapping("/increaseEnergy")
    public Result<String> increaseEnergy(@RequestParam("uid") Long uid, @RequestParam("energy") int energy) {
        userService.increaseEnergy(uid, energy);
        return Result.success("能量值增加成功");
    }

    /**
     * 增加用户能量值（RESTful风格，供FeignClient调用）
     * @param userId 用户ID
     * @param amount 增加的能量值
     * @return 操作结果
     */
    @PutMapping("/{userId}/energy")
    public Result<Void> increaseEnergyByPath(@PathVariable("userId") Long userId, @RequestParam("amount") Integer amount) {
        userService.increaseEnergy(userId, amount);
        return Result.success();
    }

    @DeleteMapping("/delete/{uid}")
    public Result<Void> deleteUser(@PathVariable Long uid) {
        userService.deleteUser(uid);
        return Result.success();
    }

    /**
     * 根据邮箱查询用户信息
     * @param email 邮箱
     * @return 用户信息
     */
    @GetMapping("/getUserByEmail/{email}")
    public Result<User> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return Result.success(user);
    }

    /**
     * 根据QQ openid查询用户信息
     * @param openid QQ openid
     * @return 用户信息
     */
    @GetMapping("/getUserByQQOpenid/{openid}")
    public Result<User> getUserByQQOpenid(@PathVariable String openid) {
        User user = userService.getUserByQQOpenid(openid);
        return Result.success(user);
    }

    /**
     * 更新用户密码
     * @param request 包含uid和password的请求
     * @return 操作结果
     */
    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@RequestBody Map<String, Object> request) {
        Long uid = Long.valueOf(String.valueOf(request.get("uid")));
        String password = String.valueOf(request.get("password"));
        userService.updatePassword(uid, password);
        return Result.success("密码更新成功");
    }

    /**
     * 创建用户（用于第三方登录自动注册）
     * @param request 包含用户信息的请求
     * @return 创建的用户信息
     */
    @PostMapping("/create")
    public Result<User> createUser(@RequestBody Map<String, Object> request) {
        try {
            User user = new User();
            
            // 设置账号（如果提供）
            if (request.get("account") != null) {
                user.setAccount(String.valueOf(request.get("account")));
            }
            
            // 设置邮箱（如果提供）
            if (request.get("email") != null) {
                user.setEmail(String.valueOf(request.get("email")));
            }
            
            // 设置QQ openid（如果提供）
            if (request.get("qqOpenid") != null) {
                user.setQqOpenid(String.valueOf(request.get("qqOpenid")));
            }
            
            // 设置微信 openid（如果提供）
            if (request.get("wxOpenid") != null) {
                user.setWxOpenid(String.valueOf(request.get("wxOpenid")));
            }
            
            // 设置密码（如果提供，需要加密）
            if (request.get("password") != null) {
                user.setPassword(String.valueOf(request.get("password")));
            }
            
            // 设置手机号（如果提供）
            if (request.get("phone") != null) {
                user.setPhone(String.valueOf(request.get("phone")));
            }
            
            // 设置状态和删除标记
            user.setStatus(request.get("status") != null ? 
                Integer.valueOf(String.valueOf(request.get("status"))) : 0);
            user.setIsdelete(request.get("isdelete") != null ? 
                Integer.valueOf(String.valueOf(request.get("isdelete"))) : 0);
            
            // 设置初始能量
            user.setEnergy(request.get("energy") != null ? 
                Integer.valueOf(String.valueOf(request.get("energy"))) : 0);
            
            // 如果没有账号，自动生成一个
            if (user.getAccount() == null || user.getAccount().isEmpty()) {
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    // 使用邮箱前缀作为账号
                    String emailPrefix = user.getEmail().split("@")[0];
                    user.setAccount("email_" + emailPrefix + "_" + System.currentTimeMillis());
                } else if (user.getQqOpenid() != null && !user.getQqOpenid().isEmpty()) {
                    user.setAccount("qq_" + user.getQqOpenid());
                } else if (user.getWxOpenid() != null && !user.getWxOpenid().isEmpty()) {
                    user.setAccount("wx_" + user.getWxOpenid());
                } else {
                    user.setAccount("user_" + System.currentTimeMillis());
                }
            }
            
            // 检查账号是否已存在
            User existingUser = userService.getUserByAccount(user.getAccount());
            if (existingUser != null) {
                return Result.fail("账号已存在");
            }
            
            // 如果提供了邮箱，检查邮箱是否已存在
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                User existingEmailUser = userService.getUserByEmail(user.getEmail());
                if (existingEmailUser != null) {
                    return Result.fail("邮箱已被注册");
                }
            }
            
            // 创建用户
            userService.createUser(user);
            
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("创建用户失败：" + e.getMessage());
        }
    }
} 
package com.project.controller;

import com.github.pagehelper.PageInfo;
import com.project.model.User;
import com.project.model.dto.UpdatePwdDTO;
import com.project.service.UserService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/delete/{uid}")
    public Result<Void> deleteUser(@PathVariable Long uid) {
        userService.deleteUser(uid);
        return Result.success();
    }
}
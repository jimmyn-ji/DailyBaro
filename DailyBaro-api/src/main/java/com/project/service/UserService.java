package com.project.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.project.mapper.UserMapper;
import com.project.model.User;
import com.project.model.dto.UpdatePwdDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 分页查询用户
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public PageInfo<User> getUsersByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userMapper.selectAllUsers();
        return new PageInfo<>(users);
    }

    public User getMyInfo(Long uid) {
        return userMapper.getMyInfo(uid);
    }

    /**
     * 根据账号查询用户信息
     * @param account 账号
     * @return 用户信息
     */
    public User getUserByAccount(String account) {
        return userMapper.selectByAccount(account);
    }

    /**
     * 修改密码
     */
    public void changePassword(UpdatePwdDTO updatePwdDTO) {
        User user = userMapper.getMyInfo(updatePwdDTO.getUid());
        if (user != null && passwordEncoder.matches(updatePwdDTO.getOldPassword(), user.getPassword())) {
            userMapper.updateUserPassword(updatePwdDTO.getUid(), passwordEncoder.encode(updatePwdDTO.getNewPassword()));
        } else {
            throw new RuntimeException("旧密码错误");
        }
    }

    /**
     * 修改个人信息
     * @param user 用户信息
     */
    public void updateUserInfo(User user) {
        userMapper.updateUserInfo(user);
    }

    /**
     * 软删除用户
     */
    public void deleteUser(Long uid) {
        User user = userMapper.getMyInfo(uid);
        if (user != null) {
            user.setIsdelete(1);
            userMapper.updateUserInfo(user);
        }
    }
}
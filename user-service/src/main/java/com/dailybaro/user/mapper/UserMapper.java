package com.dailybaro.user.mapper;

import com.dailybaro.user.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {
    List<User> selectAllUsers();

    User getMyInfo(Long uid);

    // 保留原有的方法
    User selectByAccount(String account);

    // 根据微信openid查询用户
    @Select("SELECT * FROM user WHERE wx_openid = #{openid} AND isdelete = 0")
    User selectByWxOpenid(String openid);

    // 插入用户(需包含wx_openid字段)
    @Insert("INSERT INTO user(account, password, wx_openid, phone, status, isdelete) " +
            "VALUES(#{account}, #{password}, #{wxOpenid}, #{phone}, #{status}, #{isdelete})")
    @Options(useGeneratedKeys = true, keyProperty = "uid")
    int insertUser(User user);

    /**
     * 修改用户信息
     * @param user 用户信息
     * @return 受影响的行数
     */
    int updateUserInfo(User user);

    /**
     * 修改用户密码
     * @param uid 用户 ID
     * @param newPassword 新密码
     * @return 受影响的行数
     */
    int updateUserPassword(Long uid, String newPassword);

    /**
     * 增加用户能量值
     * @param uid 用户 ID
     * @param energy 增加的能量值
     * @return 受影响的行数
     */
    @Update("UPDATE user SET energy = COALESCE(energy, 0) + #{energy} WHERE uid = #{uid}")
    int increaseEnergy(Long uid, int energy);

    /**
     * 统计用户完成的神秘盒子数量（能量值）
     * @param uid 用户 ID
     * @return 完成的神秘盒子数量
     */
    @Select("SELECT COUNT(*) FROM user_drawn_boxes WHERE user_id = #{uid} AND is_completed = 1")
    Integer countCompletedDrawnBoxes(Long uid);

    /**
     * 检查用户今天是否已经获得登录奖励
     * @param uid 用户 ID
     * @param rewardDate 奖励日期
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM user_daily_login_reward WHERE user_id = #{uid} AND reward_date = #{rewardDate}")
    Integer checkDailyLoginReward(Long uid, String rewardDate);

    /**
     * 记录用户每日登录奖励
     * @param uid 用户 ID
     * @param rewardDate 奖励日期
     * @param energyReward 能量奖励数量
     * @return 受影响的行数
     */
    @Insert("INSERT INTO user_daily_login_reward (user_id, reward_date, energy_reward) VALUES (#{uid}, #{rewardDate}, #{energyReward})")
    int recordDailyLoginReward(Long uid, String rewardDate, int energyReward);
} 
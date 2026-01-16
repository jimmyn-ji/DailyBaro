package com.project.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.mapper.MysteryBoxItemMapper;
import com.project.mapper.UserDrawnBoxMapper;
import com.project.mapper.UserMapper;
import com.project.model.MysteryBoxItem;
import com.project.model.UserDrawnBox;
import com.project.model.User;
import com.project.model.vo.MysteryBoxVO;
import com.project.service.MysteryBoxService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class MysteryBoxServiceImpl implements MysteryBoxService {

    @Autowired
    private MysteryBoxItemMapper itemMapper;
    @Autowired
    private UserDrawnBoxMapper drawnBoxMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Result<MysteryBoxVO> drawBox(Long userId) {
        // 检查用户能量值
        User user = userMapper.getMyInfo(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        
        if (user.getEnergy() == null || user.getEnergy() < 10) {
            return Result.fail("能量值不足，无法抽取盲盒。需要10点能量，完成任务可获得能量值！");
        }
        
        // 消耗10点能量
        userMapper.increaseEnergy(userId, -10);
        
        // 抽取盲盒
        List<MysteryBoxItem> allItems = itemMapper.selectList(null);
        log.info("查询盲盒数据: userId={}, totalItems={}", userId, allItems.size());
        
        if (allItems.isEmpty()) {
            // 如果抽取失败，返还能量
            userMapper.increaseEnergy(userId, 10);
            log.error("没有可用的盲盒数据: userId={}", userId);
            return Result.fail("No mystery box items available.");
        }
        
        Random random = new Random();
        MysteryBoxItem drawnItem = allItems.get(random.nextInt(allItems.size()));
        log.info("抽取盲盒: userId={}, itemId={}, content={}, type={}", 
                userId, drawnItem.getBoxItemId(), drawnItem.getContent(), drawnItem.getContentType());

        UserDrawnBox newDraw = new UserDrawnBox();
        newDraw.setUserId(userId);
        newDraw.setBoxItemId(drawnItem.getBoxItemId());
        newDraw.setDrawTime(new Date());
        newDraw.setIsCompleted(false);
        
        int insertResult = drawnBoxMapper.insert(newDraw);
        log.info("插入盲盒记录: userId={}, insertResult={}, drawnBoxId={}", 
                userId, insertResult, newDraw.getDrawnBoxId());

        return convertToVO(newDraw);
    }

    @Override
    @Transactional
    public Result<MysteryBoxVO> completeTask(Long drawnBoxId, Long userId) {
        try {
            UserDrawnBox drawnBox = drawnBoxMapper.selectById(drawnBoxId);
            if (drawnBox == null || !drawnBox.getUserId().equals(userId)) {
                return Result.fail("Drawn box not found or you don't have permission.");
            }
            
            // 如果已经完成，直接返回
            if (drawnBox.getIsCompleted()) {
                return convertToVO(drawnBox);
            }
            
            drawnBox.setIsCompleted(true);
            drawnBoxMapper.updateById(drawnBox);
            
            // 完成任务获得5点能量值奖励
            try {
                userMapper.increaseEnergy(userId, 5);
                log.info("用户完成任务获得能量奖励: userId={}, energy=5", userId);
            } catch (Exception e) {
                log.error("增加用户能量值失败: userId={}, energy=5", userId, e);
                // 即使能量值增加失败，也不影响任务完成状态
            }
            
            return convertToVO(drawnBox);
        } catch (Exception e) {
            log.error("完成任务失败: drawnBoxId={}, userId={}", drawnBoxId, userId, e);
            return Result.fail("完成任务失败: " + e.getMessage());
        }
    }

    @Override
    public Result<MysteryBoxVO> getTodayStatus(Long userId) {
        LocalDate today = LocalDate.now();
        // 使用系统默认时区而不是UTC
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        log.info("查询今日盲盒状态: userId={}, today={}, startOfDay={}, endOfDay={}", 
                userId, today, startOfDay, endOfDay);

        QueryWrapper<UserDrawnBox> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .between("draw_time", startOfDay, endOfDay)
                .orderByDesc("draw_time");
        
        List<UserDrawnBox> todayDraws = drawnBoxMapper.selectList(queryWrapper);
        
        if (todayDraws.isEmpty()) {
            log.info("未找到今日盲盒记录: userId={}", userId);
            return Result.success(null);
        }
        
        // 返回今日最新的一条记录用于显示
        log.info("找到今日盲盒记录: userId={}, count={}", userId, todayDraws.size());
        return convertToVO(todayDraws.get(0));
    }

    @Override
    public Result<List<MysteryBoxVO>> getTodayRecords(Long userId) {
        LocalDate today = LocalDate.now();
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        QueryWrapper<UserDrawnBox> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .between("draw_time", startOfDay, endOfDay)
                .orderByDesc("draw_time");
        
        List<UserDrawnBox> todayDraws = drawnBoxMapper.selectList(queryWrapper);
        
        List<MysteryBoxVO> records = todayDraws.stream()
                .map(this::convertToVO)
                .filter(result -> result.getCode() == 200)
                .map(result -> result.getData())
                .collect(java.util.stream.Collectors.toList());
        
        return Result.success(records);
    }

    @Override
    public Result<Integer> getUserEnergy(Long userId) {
        try {
            User user = userMapper.getMyInfo(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            return Result.success(user.getEnergy() != null ? user.getEnergy() : 0);
        } catch (Exception e) {
            log.error("获取用户能量值失败: userId={}", userId, e);
            return Result.fail("获取能量值失败");
        }
    }

    private Result<MysteryBoxVO> convertToVO(UserDrawnBox drawnBox) {
        try {
            MysteryBoxItem item = itemMapper.selectById(drawnBox.getBoxItemId());
            if (item == null) {
                log.error("盲盒内容不存在: drawnBoxId={}, boxItemId={}", 
                        drawnBox.getDrawnBoxId(), drawnBox.getBoxItemId());
                return Result.fail("Mystery box item not found.");
            }

            MysteryBoxVO vo = new MysteryBoxVO();
            vo.setDrawnBoxId(drawnBox.getDrawnBoxId());
            vo.setContent(item.getContent());
            vo.setContentType(item.getContentType());
            vo.setIsCompleted(drawnBox.getIsCompleted());
            vo.setDrawTime(drawnBox.getDrawTime());
            vo.setEnergyReward(drawnBox.getIsCompleted() ? 5 : 0); // 完成任务奖励5点能量

            log.info("转换盲盒VO成功: drawnBoxId={}, content={}, type={}", 
                    drawnBox.getDrawnBoxId(), item.getContent(), item.getContentType());
            return Result.success(vo);
        } catch (Exception e) {
            log.error("转换盲盒VO失败: drawnBoxId={}", drawnBox.getDrawnBoxId(), e);
            return Result.fail("转换盲盒数据失败: " + e.getMessage());
        }
    }
} 
package com.project.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.mapper.EmotionCapsuleMapper;
import com.project.mapper.UserMapper;
import com.project.model.EmotionCapsule;
import com.project.model.User;
import com.project.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class EmotionCapsuleReminderTask {

    @Autowired
    private EmotionCapsuleMapper capsuleMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailService mailService;

    // 每5分钟执行一次，更频繁地检查
    @Scheduled(cron = "0 */5 * * * ?")
    public void sendReminders() {
        Date now = new Date();
        QueryWrapper<EmotionCapsule> query = new QueryWrapper<>();
        query.le("open_time", now).eq("reminder_sent", 0);

        List<EmotionCapsule> dueCapsules = capsuleMapper.selectList(query);
        for (EmotionCapsule capsule : dueCapsules) {
            // 查找用户邮箱
            User user = userMapper.getMyInfo(capsule.getUserId());
            if (user != null && user.getEmail() != null) {
                String to = user.getEmail();
                String subject = "情绪胶囊开启提醒";
                String content = "您的情绪胶囊已到开启时间，请登录App查看！";
                try {
                    mailService.sendSimpleMail(to, subject, content);
                    log.info("已发送邮件提醒给用户: {}, 胶囊ID: {}", user.getAccount(), capsule.getCapsuleId());
                } catch (Exception e) {
                    log.error("发送邮件提醒失败: {}", e.getMessage());
                }
            }
            // 标记为已发送提醒
            capsule.setReminderSent(1);
            capsuleMapper.updateById(capsule);
        }
        if (dueCapsules.size() > 0) {
            log.info("情绪胶囊提醒任务执行完毕，提醒数量：{}", dueCapsules.size());
        }
    }

    // 每天凌晨1点执行一次，作为备用检查
    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyReminderCheck() {
        log.info("执行每日情绪胶囊提醒检查");
        sendReminders();
    }
} 
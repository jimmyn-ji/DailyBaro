package com.project.util;

import com.project.mapper.MysteryBoxItemMapper;
import com.project.mapper.DailyQuoteMapper;
import com.project.model.MysteryBoxItem;
import com.project.model.DailyQuote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private MysteryBoxItemMapper mysteryBoxItemMapper;

    @Autowired
    private DailyQuoteMapper dailyQuoteMapper;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化数据库数据...");
        
        // 初始化盲盒数据
        initMysteryBoxItems();
        
        // 初始化日签数据
        initDailyQuotes();
        
        log.info("数据库数据初始化完成！");
    }

    private void initMysteryBoxItems() {
        try {
            // 检查是否已有数据
            long count = mysteryBoxItemMapper.selectCount(null);
            if (count > 0) {
                log.info("盲盒数据已存在，跳过初始化");
                return;
            }

            log.info("开始初始化盲盒数据...");

            // 治愈语录
            List<String> quotes = Arrays.asList(
                "今天的你比昨天更棒！",
                "每一个微笑都是对生活的热爱",
                "相信自己，你比想象中更强大",
                "慢慢来，一切都会好起来的",
                "今天的阳光正好，心情也要美美的",
                "你是独一无二的，值得被温柔对待",
                "每一个努力的日子都不会被辜负",
                "保持微笑，生活会更美好",
                "今天的你依然闪闪发光",
                "相信自己，你正在成为更好的自己"
            );

            // 有趣任务
            List<String> tasks = Arrays.asList(
                "今天尝试做一件一直想做但没做的小事",
                "给一个朋友发个温暖的问候",
                "整理一下自己的房间或工作台",
                "尝试一种新的食物或饮料",
                "拍一张今天最美的照片",
                "给家人打个电话或发个消息",
                "学习一个新的小技能",
                "写下一件今天感恩的事情",
                "给陌生人一个微笑",
                "尝试一个新的运动或活动",
                "听一首从未听过的音乐",
                "画一幅简单的画",
                "给植物浇水或照顾宠物",
                "尝试一个新的发型或穿搭",
                "给未来的自己写一封信"
            );

            // 情绪技巧
            List<String> tips = Arrays.asList(
                "深呼吸三次，感受当下的平静",
                "写下三件今天让你感恩的事情",
                "听一首喜欢的音乐，放松心情",
                "给自己一个温暖的拥抱",
                "闭上眼睛，想象美好的画面",
                "做5分钟的简单拉伸运动",
                "喝一杯温水，感受温暖",
                "数数周围的五种颜色",
                "深呼吸，感受空气的流动",
                "给自己一个鼓励的话语",
                "想象自己在一个安全的地方",
                "做几个简单的瑜伽动作",
                "听一段自然的声音",
                "写下今天的三个小成就",
                "给自己一个微笑，即使心情不好"
            );

            // 激励挑战
            List<String> challenges = Arrays.asList(
                "今天挑战自己，做一件平时不敢做的事情",
                "尝试一个新的爱好或兴趣",
                "主动和陌生人聊天",
                "尝试一个新的学习方法",
                "挑战自己的舒适区",
                "尝试一个新的表达方式",
                "挑战自己的创意极限",
                "尝试一个新的思维方式",
                "挑战自己的耐心极限",
                "尝试一个新的生活节奏"
            );

            // 治愈活动
            List<String> activities = Arrays.asList(
                "泡一杯花茶，享受宁静时光",
                "看一部温暖的电影",
                "听一段治愈的播客",
                "做一次手工艺创作",
                "读一本喜欢的书",
                "做一次冥想练习",
                "听一场音乐会",
                "做一次园艺活动",
                "写一首小诗",
                "做一次手工烘焙"
            );

            // 社交互动
            List<String> socials = Arrays.asList(
                "主动联系一个老朋友",
                "参加一个社交活动",
                "给同事或同学一个赞美",
                "分享一个有趣的故事",
                "邀请朋友一起做某事",
                "给陌生人一个帮助",
                "参加一个志愿者活动",
                "给家人一个惊喜",
                "和朋友分享一个笑话",
                "给身边的人一个拥抱"
            );

            // 插入数据
            insertMysteryBoxItems("quote", quotes);
            insertMysteryBoxItems("task", tasks);
            insertMysteryBoxItems("tip", tips);
            insertMysteryBoxItems("challenge", challenges);
            insertMysteryBoxItems("activity", activities);
            insertMysteryBoxItems("social", socials);

            log.info("盲盒数据初始化完成，共插入 {} 条数据", 
                mysteryBoxItemMapper.selectCount(null));

        } catch (Exception e) {
            log.error("初始化盲盒数据失败", e);
        }
    }

    private void insertMysteryBoxItems(String contentType, List<String> contents) {
        for (String content : contents) {
            MysteryBoxItem item = new MysteryBoxItem();
            item.setContentType(contentType);
            item.setContent(content);
            mysteryBoxItemMapper.insert(item);
        }
    }

    private void initDailyQuotes() {
        try {
            // 检查是否已有数据
            long count = dailyQuoteMapper.selectCount(null);
            if (count > 0) {
                log.info("日签数据已存在，跳过初始化");
                return;
            }

            log.info("开始初始化日签数据...");

            // 日签内容
            List<String> quotes = Arrays.asList(
                "今天也值得被温柔对待",
                "每一个微笑都是对生活的热爱",
                "相信自己，你比想象中更强大",
                "慢慢来，一切都会好起来的",
                "今天的阳光正好，心情也要美美的",
                "你是独一无二的，值得被温柔对待",
                "每一个努力的日子都不会被辜负",
                "保持微笑，生活会更美好",
                "今天的你依然闪闪发光",
                "相信自己，你正在成为更好的自己",
                "生活不会亏待每一个努力的人",
                "今天的你比昨天更棒",
                "每一个清晨都是新的开始",
                "相信自己，你值得拥有美好",
                "今天的你依然值得被爱",
                "每一个梦想都值得被追逐",
                "生活总是充满希望",
                "今天的你依然勇敢",
                "相信自己，你正在发光",
                "每一个瞬间都值得珍惜",
                "今天的你依然美丽",
                "生活不会辜负每一个善良的人",
                "相信自己，你正在成长",
                "每一个选择都值得被尊重",
                "今天的你依然坚强",
                "生活总是充满惊喜",
                "相信自己，你正在进步",
                "每一个努力都值得被认可",
                "今天的你依然可爱",
                "生活不会亏待每一个真诚的人",
                "相信自己，你正在改变",
                "每一个微笑都值得被珍藏",
                "今天的你依然温暖",
                "生活总是充满可能",
                "相信自己，你正在绽放",
                "每一个瞬间都值得被记录",
                "今天的你依然美好",
                "生活不会辜负每一个坚持的人",
                "相信自己，你正在闪耀",
                "每一个选择都值得被坚持",
                "今天的你依然独特",
                "生活总是充满温暖",
                "相信自己，你正在发光发热",
                "每一个努力都值得被赞美",
                "今天的你依然值得被珍惜",
                "生活不会亏待每一个勇敢的人",
                "相信自己，你正在创造奇迹",
                "每一个微笑都值得被传递",
                "今天的你依然充满希望",
                "生活总是充满爱",
                "相信自己，你正在改变世界",
                "每一个瞬间都值得被感恩",
                "今天的你依然充满力量",
                "生活不会辜负每一个善良的心",
                "相信自己，你正在影响他人",
                "每一个选择都值得被庆祝",
                "今天的你依然充满魅力",
                "生活总是充满美好",
                "相信自己，你正在创造价值",
                "每一个努力都值得被铭记",
                "今天的你依然充满活力",
                "生活不会亏待每一个真诚的心",
                "相信自己，你正在实现梦想",
                "每一个微笑都值得被分享",
                "今天的你依然充满智慧",
                "生活总是充满惊喜和感动",
                "相信自己，你正在成为榜样",
                "每一个瞬间都值得被珍惜和回忆",
                "今天的你依然充满勇气",
                "生活不会辜负每一个努力追求的人",
                "相信自己，你正在创造属于自己的精彩"
            );

            // 插入日签数据
            for (String content : quotes) {
                DailyQuote quote = new DailyQuote();
                quote.setContent(content);
                quote.setAuthor("系统");
                dailyQuoteMapper.insert(quote);
            }

            log.info("日签数据初始化完成，共插入 {} 条数据", 
                dailyQuoteMapper.selectCount(null));

        } catch (Exception e) {
            log.error("初始化日签数据失败", e);
        }
    }
} 
/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 90200 (9.2.0)
 Source Host           : localhost:3306
 Source Schema         : dailybaro

 Target Server Type    : MySQL
 Target Server Version : 90200 (9.2.0)
 File Encoding         : 65001

 Date: 17/01/2026 03:21:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for anonymous_posts
-- ----------------------------
DROP TABLE IF EXISTS `anonymous_posts`;
CREATE TABLE `anonymous_posts` (
  `post_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `content` text NOT NULL,
  `visibility` enum('public','private') DEFAULT 'public',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `anonymous_posts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of anonymous_posts
-- ----------------------------
BEGIN;
INSERT INTO `anonymous_posts` (`post_id`, `user_id`, `content`, `visibility`, `create_time`) VALUES (3, 99999, '欢迎来到匿名星球', 'public', '2025-07-26 03:17:17');
INSERT INTO `anonymous_posts` (`post_id`, `user_id`, `content`, `visibility`, `create_time`) VALUES (4, 99999, '说点什么', 'public', '2025-07-26 14:34:58');
INSERT INTO `anonymous_posts` (`post_id`, `user_id`, `content`, `visibility`, `create_time`) VALUES (5, 99999, '测试一下🐬', 'private', '2025-07-26 14:58:32');
INSERT INTO `anonymous_posts` (`post_id`, `user_id`, `content`, `visibility`, `create_time`) VALUES (6, 1, '希望音频测试顺利通过😊', 'public', '2025-07-26 23:00:00');
INSERT INTO `anonymous_posts` (`post_id`, `user_id`, `content`, `visibility`, `create_time`) VALUES (7, 1, '说点什么🌈', 'public', '2025-07-26 23:50:55');
INSERT INTO `anonymous_posts` (`post_id`, `user_id`, `content`, `visibility`, `create_time`) VALUES (8, 1, '1', 'public', '2025-07-29 14:33:41');
INSERT INTO `anonymous_posts` (`post_id`, `user_id`, `content`, `visibility`, `create_time`) VALUES (9, 100001, '1', 'public', '2025-08-06 18:07:47');
INSERT INTO `anonymous_posts` (`post_id`, `user_id`, `content`, `visibility`, `create_time`) VALUES (10, 100001, '1', 'private', '2025-08-06 18:13:16');
INSERT INTO `anonymous_posts` (`post_id`, `user_id`, `content`, `visibility`, `create_time`) VALUES (11, 1, '1', 'private', '2025-08-06 18:14:01');
INSERT INTO `anonymous_posts` (`post_id`, `user_id`, `content`, `visibility`, `create_time`) VALUES (12, 1, '向宇宙发送我的信号', 'public', '2025-08-14 14:08:56');
COMMIT;

-- ----------------------------
-- Table structure for capsule_media
-- ----------------------------
DROP TABLE IF EXISTS `capsule_media`;
CREATE TABLE `capsule_media` (
  `media_id` bigint NOT NULL AUTO_INCREMENT,
  `capsule_id` bigint NOT NULL,
  `media_type` varchar(32) DEFAULT NULL,
  `media_url` varchar(500) NOT NULL,
  PRIMARY KEY (`media_id`),
  KEY `capsule_id` (`capsule_id`),
  CONSTRAINT `capsule_media_ibfk_1` FOREIGN KEY (`capsule_id`) REFERENCES `emotion_capsules` (`capsule_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of capsule_media
-- ----------------------------
BEGIN;
INSERT INTO `capsule_media` (`media_id`, `capsule_id`, `media_type`, `media_url`) VALUES (28, 26, 'audio', '/uploads/1aaf466b-2a64-4387-a827-f3ae2ee04c4f.mp3');
INSERT INTO `capsule_media` (`media_id`, `capsule_id`, `media_type`, `media_url`) VALUES (43, 44, 'audio', '/uploads/d4287a3a-5d8e-45ec-8d62-fac5a7191fd5.mp3');
INSERT INTO `capsule_media` (`media_id`, `capsule_id`, `media_type`, `media_url`) VALUES (44, 57, 'audio', '/uploads/e62f66f4-1388-47fd-917f-cb37b46af510.mp3');
INSERT INTO `capsule_media` (`media_id`, `capsule_id`, `media_type`, `media_url`) VALUES (56, 85, 'video', '/uploads/8b173ceb-c340-4fd0-be33-25077ed25866.mp4');
INSERT INTO `capsule_media` (`media_id`, `capsule_id`, `media_type`, `media_url`) VALUES (57, 86, 'video', '/uploads/703db240-62f0-4b9d-b097-252d87739af0.mp4');
COMMIT;

-- ----------------------------
-- Table structure for daily_quotes
-- ----------------------------
DROP TABLE IF EXISTS `daily_quotes`;
CREATE TABLE `daily_quotes` (
  `quote_id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(500) NOT NULL,
  `author` varchar(100) DEFAULT 'Unknown',
  PRIMARY KEY (`quote_id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of daily_quotes
-- ----------------------------
BEGIN;
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (1, '今天也值得被温柔对待', '系统');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (2, '你很棒，继续加油！', '系统');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (3, '每一天都是新的开始', '系统');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (4, '保持微笑，生活会更美好', '系统');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (5, '今天也值得被温柔对待', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (6, '每一个微笑都是对生活的热爱', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (7, '相信自己，你比想象中更强大', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (8, '今天的努力是明天的收获', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (9, '保持希望，保持微笑', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (10, '你正在成为更好的自己', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (11, '每一个今天都是新的开始', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (12, '温柔对待自己，也温柔对待他人', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (13, '小小的进步也是值得庆祝的', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (14, '你的存在本身就是一种美好', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (15, '今天也要开开心心的', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (16, '相信自己，你可以的', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (17, '每一个挑战都是成长的机会', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (18, '保持初心，保持热爱', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (19, '今天的你比昨天更优秀', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (20, '温柔的力量最强大', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (21, '相信自己内心的声音', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (22, '每一个选择都让你更接近梦想', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (23, '今天的阳光为你而亮', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (24, '你值得拥有所有的美好', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (25, '今天也值得被温柔对待', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (26, '每一个微笑都是对生活的热爱', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (27, '相信自己，你比想象中更强大', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (28, '今天的努力是明天的收获', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (29, '保持希望，保持微笑', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (30, '你正在成为更好的自己', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (31, '每一个今天都是新的开始', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (32, '温柔对待自己，也温柔对待他人', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (33, '小小的进步也是值得庆祝的', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (34, '你的存在本身就是一种美好', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (35, '今天也要开开心心的', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (36, '相信自己，你可以的', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (37, '每一个挑战都是成长的机会', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (38, '保持初心，保持热爱', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (39, '今天的你比昨天更优秀', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (40, '温柔的力量最强大', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (41, '相信自己内心的声音', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (42, '每一个选择都让你更接近梦想', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (43, '今天的阳光为你而亮', 'Unknown');
INSERT INTO `daily_quotes` (`quote_id`, `content`, `author`) VALUES (44, '你值得拥有所有的美好', 'Unknown');
COMMIT;

-- ----------------------------
-- Table structure for diaries
-- ----------------------------
DROP TABLE IF EXISTS `diaries`;
CREATE TABLE `diaries` (
  `diary_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `content` text,
  `status` enum('draft','published') DEFAULT 'draft',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`diary_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `diaries_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of diaries
-- ----------------------------
BEGIN;
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (2, 1, 'ceshih', 'ces', 'published', '2025-07-25 15:17:09', '2026-01-15 03:05:46');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (6, 100000, '这是一个测试', '这是一个测试', 'draft', '2025-07-28 23:00:17', '2025-08-08 16:10:14');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (7, 1, '听力day1', '平静', 'published', '2025-08-05 18:55:03', '2025-08-22 02:21:05');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (11, 100001, '测试', '测试', 'draft', '2025-08-07 22:10:38', '2025-08-09 15:39:17');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (12, 100000, '今天的情绪盲盒抽不到我想要的内容', '生气...', 'published', '2025-08-08 16:12:57', '2025-08-08 16:12:57');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (13, 100001, '这是一个测试日记', '希望测试顺利通过', 'draft', '2025-08-10 01:20:36', '2025-08-10 01:20:36');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (14, 100001, '测试', '测试', 'draft', '2025-08-10 01:49:01', '2025-08-10 01:49:01');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (18, 100007, NULL, NULL, 'draft', '2025-08-22 17:32:44', '2025-08-22 17:32:44');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (19, 100007, NULL, NULL, 'draft', '2025-08-22 17:39:42', '2025-08-22 17:39:42');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (20, 100007, NULL, NULL, 'draft', '2025-08-22 17:44:54', '2025-08-22 17:44:54');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (21, 100007, NULL, NULL, 'draft', '2025-08-22 17:45:39', '2025-08-22 17:45:39');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (22, 100007, '题目A', '表单内容XYZ', 'published', '2025-08-22 17:46:32', '2025-08-22 17:46:32');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (23, 1, 'hi', 'hihihihihihihihihihihihihihihi', 'draft', '2026-01-15 02:38:31', '2026-01-16 03:23:02');
INSERT INTO `diaries` (`diary_id`, `user_id`, `title`, `content`, `status`, `create_time`, `update_time`) VALUES (24, 1, '纪念一下smtp接入成功', '纪念一下smtp接入成功，rag待测试', 'published', '2026-01-17 00:42:10', '2026-01-17 00:42:42');
COMMIT;

-- ----------------------------
-- Table structure for diary_media
-- ----------------------------
DROP TABLE IF EXISTS `diary_media`;
CREATE TABLE `diary_media` (
  `media_id` bigint NOT NULL AUTO_INCREMENT,
  `diary_id` bigint NOT NULL,
  `media_type` enum('image','video','audio') NOT NULL,
  `media_url` varchar(500) NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`media_id`),
  KEY `diary_id` (`diary_id`),
  CONSTRAINT `diary_media_ibfk_1` FOREIGN KEY (`diary_id`) REFERENCES `diaries` (`diary_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of diary_media
-- ----------------------------
BEGIN;
INSERT INTO `diary_media` (`media_id`, `diary_id`, `media_type`, `media_url`, `create_time`) VALUES (3, 2, 'image', '/uploads/c7231325-a491-406f-a6b3-ba07d96bcbec.png', '2025-07-25 20:16:56');
INSERT INTO `diary_media` (`media_id`, `diary_id`, `media_type`, `media_url`, `create_time`) VALUES (12, 6, 'video', '/uploads/77fffe49-5211-412a-934d-a253b88fd4b3.mp4', '2025-07-28 23:58:39');
INSERT INTO `diary_media` (`media_id`, `diary_id`, `media_type`, `media_url`, `create_time`) VALUES (15, 11, 'audio', '/uploads/3550c916-38fb-4859-9810-ebc52447f33f.mp3', '2025-08-07 22:10:37');
INSERT INTO `diary_media` (`media_id`, `diary_id`, `media_type`, `media_url`, `create_time`) VALUES (16, 11, 'video', '/uploads/409848a1-51e2-4fc4-ab7d-ce1131b041e4.mp4', '2025-08-07 22:10:37');
INSERT INTO `diary_media` (`media_id`, `diary_id`, `media_type`, `media_url`, `create_time`) VALUES (17, 12, 'audio', '/uploads/536329fd-a734-44f4-9c0a-2c74ff63982f.mp3', '2025-08-08 16:12:56');
INSERT INTO `diary_media` (`media_id`, `diary_id`, `media_type`, `media_url`, `create_time`) VALUES (18, 13, 'audio', '/uploads/f3a3717f-b492-4a2f-8a53-5f68a560fa86.mp3', '2025-08-10 01:20:36');
INSERT INTO `diary_media` (`media_id`, `diary_id`, `media_type`, `media_url`, `create_time`) VALUES (19, 13, 'video', '/uploads/3e6b6119-d476-4ef5-9674-ed01343c3d1c.mp4', '2025-08-10 01:20:36');
INSERT INTO `diary_media` (`media_id`, `diary_id`, `media_type`, `media_url`, `create_time`) VALUES (20, 14, 'video', '/uploads/3481c044-ae49-455f-81f7-47a894f1099e.mp4', '2025-08-10 01:49:01');
INSERT INTO `diary_media` (`media_id`, `diary_id`, `media_type`, `media_url`, `create_time`) VALUES (23, 23, 'audio', '/uploads/6f1325b6-2080-44f7-aee1-f4bfcfe4694d.ogg', '2026-01-15 02:38:30');
COMMIT;

-- ----------------------------
-- Table structure for diary_tags
-- ----------------------------
DROP TABLE IF EXISTS `diary_tags`;
CREATE TABLE `diary_tags` (
  `diary_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`diary_id`,`tag_id`),
  KEY `tag_id` (`tag_id`),
  CONSTRAINT `diary_tags_ibfk_1` FOREIGN KEY (`diary_id`) REFERENCES `diaries` (`diary_id`) ON DELETE CASCADE,
  CONSTRAINT `diary_tags_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`tag_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of diary_tags
-- ----------------------------
BEGIN;
INSERT INTO `diary_tags` (`diary_id`, `tag_id`) VALUES (6, 12);
INSERT INTO `diary_tags` (`diary_id`, `tag_id`) VALUES (12, 13);
INSERT INTO `diary_tags` (`diary_id`, `tag_id`) VALUES (14, 13);
INSERT INTO `diary_tags` (`diary_id`, `tag_id`) VALUES (11, 14);
INSERT INTO `diary_tags` (`diary_id`, `tag_id`) VALUES (13, 15);
COMMIT;

-- ----------------------------
-- Table structure for emotion_analysis_result
-- ----------------------------
DROP TABLE IF EXISTS `emotion_analysis_result`;
CREATE TABLE `emotion_analysis_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `analysis_text` text,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `emotion_analysis_result_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`uid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of emotion_analysis_result
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for emotion_capsules
-- ----------------------------
DROP TABLE IF EXISTS `emotion_capsules`;
CREATE TABLE `emotion_capsules` (
  `capsule_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `content` text,
  `current_emotion` varchar(50) DEFAULT '开心' COMMENT '当前情绪',
  `thoughts` text COMMENT '想法',
  `future_goal` text COMMENT '未来目标',
  `open_time` timestamp NOT NULL,
  `reminder_type` enum('app_notification','sms') DEFAULT 'app_notification',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `reminder_sent` tinyint(1) DEFAULT '0',
  `reminder_read` tinyint(1) DEFAULT '0' COMMENT '应用内提醒是否已读 0未读 1已读',
  PRIMARY KEY (`capsule_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `emotion_capsules_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of emotion_capsules
-- ----------------------------
BEGIN;
INSERT INTO `emotion_capsules` (`capsule_id`, `user_id`, `content`, `current_emotion`, `thoughts`, `future_goal`, `open_time`, `reminder_type`, `create_time`, `reminder_sent`, `reminder_read`) VALUES (26, 100000, '测试', '开心', '测试', '测试', '2025-07-27 18:38:00', 'app_notification', '2025-07-27 18:37:03', 1, 0);
INSERT INTO `emotion_capsules` (`capsule_id`, `user_id`, `content`, `current_emotion`, `thoughts`, `future_goal`, `open_time`, `reminder_type`, `create_time`, `reminder_sent`, `reminder_read`) VALUES (44, 100000, '1', 'happy', '1', '1', '2025-08-08 16:12:00', 'app_notification', '2025-08-08 16:11:52', 1, 0);
INSERT INTO `emotion_capsules` (`capsule_id`, `user_id`, `content`, `current_emotion`, `thoughts`, `future_goal`, `open_time`, `reminder_type`, `create_time`, `reminder_sent`, `reminder_read`) VALUES (50, 100000, '1', 'happy', '1', '1', '2025-08-08 22:09:00', 'app_notification', '2025-08-08 22:08:10', 1, 0);
INSERT INTO `emotion_capsules` (`capsule_id`, `user_id`, `content`, `current_emotion`, `thoughts`, `future_goal`, `open_time`, `reminder_type`, `create_time`, `reminder_sent`, `reminder_read`) VALUES (57, 100001, '1', 'happy', '1', '1', '2025-08-09 18:10:00', 'app_notification', '2025-08-09 18:10:14', 1, 1);
INSERT INTO `emotion_capsules` (`capsule_id`, `user_id`, `content`, `current_emotion`, `thoughts`, `future_goal`, `open_time`, `reminder_type`, `create_time`, `reminder_sent`, `reminder_read`) VALUES (85, 1, '1', 'happy', '1', '1', '2025-08-11 15:28:00', 'app_notification', '2025-08-11 15:27:44', 1, 1);
INSERT INTO `emotion_capsules` (`capsule_id`, `user_id`, `content`, `current_emotion`, `thoughts`, `future_goal`, `open_time`, `reminder_type`, `create_time`, `reminder_sent`, `reminder_read`) VALUES (86, 1, '11', 'happy', '11', '1', '2025-08-11 15:46:00', 'app_notification', '2025-08-11 15:45:48', 1, 0);
COMMIT;

-- ----------------------------
-- Table structure for knowledge
-- ----------------------------
DROP TABLE IF EXISTS `knowledge`;
CREATE TABLE `knowledge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `category` varchar(50) NOT NULL COMMENT '分类：情绪管理、心理排忧、成长指南、案例分享',
  `subcategory` varchar(50) DEFAULT NULL COMMENT '子分类：如焦虑、抑郁、压力等',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签（逗号分隔）',
  `vector_id` varchar(100) DEFAULT NULL COMMENT 'Milvus向量ID',
  `status` int DEFAULT '0' COMMENT '状态：0-草稿，1-已发布，2-已审核',
  `view_count` int DEFAULT '0' COMMENT '浏览次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_subcategory` (`category`,`subcategory`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='心理健康知识库表';

-- ----------------------------
-- Records of knowledge
-- ----------------------------
BEGIN;
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (1, '如何应对焦虑情绪', '焦虑是一种常见的情绪反应，当我们面临压力、不确定性或挑战时，焦虑会自然产生。以下是一些应对焦虑的方法：\n\n1. 深呼吸练习：当感到焦虑时，尝试进行深呼吸。慢慢地吸气，数到4，然后屏住呼吸数到4，再慢慢呼气数到4。重复几次，可以帮助身体放松。\n\n2. 正念冥想：专注于当下，不要过度担心未来。可以通过冥想、瑜伽等方式练习正念。\n\n3. 规律作息：保持规律的作息时间，充足的睡眠有助于情绪稳定。\n\n4. 适度运动：定期进行有氧运动，如散步、跑步、游泳等，可以释放压力，改善情绪。\n\n5. 寻求支持：与信任的朋友、家人或专业人士分享你的感受，不要独自承受。\n\n如果焦虑情绪持续影响日常生活，建议寻求专业心理咨询师的帮助。', '情绪管理', '焦虑', '焦虑,情绪管理,心理健康,应对方法', NULL, 1, 1, '2026-01-16 17:21:51', '2026-01-16 19:22:38', NULL);
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (2, '焦虑的认知行为疗法', '认知行为疗法（CBT）是治疗焦虑的有效方法之一。它通过改变不合理的思维模式和行为习惯来缓解焦虑：\n\n1. 识别焦虑触发因素：记录引发焦虑的具体情境、想法和感受。\n\n2. 挑战负面思维：当你感到焦虑时，问自己：\n   - 这个想法有证据支持吗？\n   - 最坏的情况真的会发生吗？\n   - 有没有更合理的解释？\n\n3. 行为实验：逐步面对让你焦虑的情境，验证你的担心是否真实。\n\n4. 放松技巧：学习渐进式肌肉放松、想象放松等技巧。\n\n5. 建立应对策略：制定具体的应对计划，包括如何应对焦虑发作。\n\n记住，改变需要时间和练习，不要对自己过于苛刻。', '情绪管理', '焦虑', '焦虑,认知行为疗法,CBT,心理治疗', NULL, 1, 0, '2026-01-16 17:21:51', '2026-01-16 17:21:51', NULL);
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (3, '认识抑郁症', '抑郁症是一种常见的心理健康问题，不仅仅是\"心情不好\"。它会影响人的思维、情绪、行为和身体健康。\n\n主要症状包括：\n- 持续的情绪低落、悲伤或空虚感\n- 对以前喜欢的活动失去兴趣\n- 食欲或体重明显变化\n- 睡眠问题（失眠或过度睡眠）\n- 疲劳或精力不足\n- 注意力难以集中\n- 自我价值感降低或过度内疚\n- 反复出现死亡或自杀的想法\n\n如果你或你认识的人出现这些症状，特别是持续两周以上，建议寻求专业帮助。抑郁症是可以治疗的，不要独自承受。', '情绪管理', '抑郁', '抑郁,抑郁症,心理健康,症状识别', NULL, 1, 0, '2026-01-16 17:21:51', '2026-01-16 17:21:51', NULL);
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (4, '如何帮助抑郁的朋友', '如果你的朋友或家人正在经历抑郁，你的支持非常重要。以下是一些建议：\n\n1. 倾听而不评判：让ta知道你在乎，愿意倾听。不要试图\"解决\"问题或说\"振作起来\"。\n\n2. 表达关心：定期联系，即使ta可能不想回应。让ta知道你不期望什么，只是关心ta。\n\n3. 鼓励寻求帮助：温和地建议ta寻求专业帮助，可以提供一些资源或陪同前往。\n\n4. 保持耐心：抑郁症的恢复需要时间，不要期望立即见效。\n\n5. 照顾好自己：支持抑郁的人可能会让你感到疲惫，记得也要照顾好自己的心理健康。\n\n6. 了解危机信号：如果ta提到自杀或自伤，立即寻求紧急帮助。\n\n记住，你不是治疗师，但你的支持可以带来巨大的改变。', '情绪管理', '抑郁', '抑郁,支持,帮助他人,心理健康', NULL, 1, 0, '2026-01-16 17:21:51', '2026-01-16 17:21:51', NULL);
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (5, '压力管理技巧', '压力是生活的一部分，但过度的压力会影响身心健康。以下是一些有效的压力管理技巧：\n\n1. 时间管理：\n   - 制定优先级清单\n   - 学会说\"不\"\n   - 避免过度承诺\n   - 合理规划时间\n\n2. 放松技巧：\n   - 深呼吸练习\n   - 渐进式肌肉放松\n   - 冥想或正念练习\n   - 听舒缓的音乐\n\n3. 生活方式调整：\n   - 保持规律作息\n   - 均衡饮食\n   - 适度运动\n   - 限制咖啡因和酒精\n\n4. 社交支持：\n   - 与朋友家人保持联系\n   - 参加支持小组\n   - 寻求专业帮助\n\n5. 改变思维方式：\n   - 接受无法控制的事情\n   - 关注积极方面\n   - 保持现实期望\n\n记住，压力管理是一个持续的过程，找到适合你的方法最重要。', '情绪管理', '压力', '压力,压力管理,应对技巧,心理健康', NULL, 1, 0, '2026-01-16 17:21:51', '2026-01-16 17:21:51', NULL);
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (6, '如何处理失眠问题', '失眠是许多人面临的常见问题，可能由压力、焦虑、生活习惯等多种因素引起。以下是一些改善睡眠的方法：\n\n1. 建立规律的睡眠时间表：\n   - 每天在同一时间上床和起床\n   - 即使在周末也保持规律\n\n2. 创造良好的睡眠环境：\n   - 保持卧室凉爽、黑暗、安静\n   - 使用遮光窗帘或眼罩\n   - 确保床垫和枕头舒适\n\n3. 睡前习惯：\n   - 睡前1小时避免使用电子设备\n   - 进行放松活动，如阅读、听音乐\n   - 避免睡前大餐或大量饮水\n\n4. 白天习惯：\n   - 限制午睡时间（不超过30分钟）\n   - 定期运动，但避免睡前剧烈运动\n   - 限制咖啡因摄入，特别是下午\n\n5. 如果无法入睡：\n   - 不要强迫自己入睡\n   - 如果20分钟后仍清醒，起床做些放松活动\n   - 避免看时间，这会增加焦虑\n\n如果失眠持续影响生活，建议咨询医生或睡眠专家。', '心理排忧', '常见问题', '失眠,睡眠,健康,生活习惯', NULL, 1, 0, '2026-01-16 17:21:51', '2026-01-16 17:21:51', NULL);
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (7, '如何提高自信心', '自信心是心理健康的重要组成部分。以下是一些提高自信心的方法：\n\n1. 认识自己的优点：\n   - 列出你的优点和成就\n   - 定期回顾，提醒自己有价值\n\n2. 设定现实目标：\n   - 制定可达成的小目标\n   - 每完成一个目标，给自己奖励\n\n3. 接受不完美：\n   - 没有人是完美的\n   - 错误是学习的机会\n   - 对自己保持善意\n\n4. 积极自我对话：\n   - 注意内心的声音\n   - 用积极的话语替代负面想法\n   - 像对待朋友一样对待自己\n\n5. 照顾身体：\n   - 保持健康的生活方式\n   - 穿着让你感觉良好的衣服\n   - 保持良好的姿态\n\n6. 学习新技能：\n   - 挑战自己学习新事物\n   - 掌握新技能会增强自信\n\n7. 帮助他人：\n   - 志愿服务或帮助他人\n   - 这会让你感到有价值\n\n记住，自信心的建立需要时间，对自己保持耐心。', '心理排忧', '常见问题', '自信,自信心,自我提升,心理健康', NULL, 1, 0, '2026-01-16 17:21:51', '2026-01-16 17:21:51', NULL);
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (8, '情绪自我调节方法', '情绪自我调节是管理情绪反应的重要技能。以下是一些实用的方法：\n\n1. 情绪识别：\n   - 学会识别和命名你的情绪\n   - 理解情绪产生的原因\n   - 接受情绪是正常的\n\n2. 深呼吸和放松：\n   - 当情绪激动时，先深呼吸\n   - 使用4-7-8呼吸法：吸气4秒，屏息7秒，呼气8秒\n   - 进行渐进式肌肉放松\n\n3. 认知重构：\n   - 挑战负面思维\n   - 寻找更平衡的观点\n   - 关注可以控制的事情\n\n4. 情绪表达：\n   - 通过写作、艺术等方式表达情绪\n   - 与信任的人分享感受\n   - 避免压抑情绪\n\n5. 转移注意力：\n   - 从事喜欢的活动\n   - 听音乐、阅读、运动\n   - 暂时离开引发情绪的情境\n\n6. 问题解决：\n   - 识别问题的根源\n   - 制定解决方案\n   - 采取行动\n\n7. 自我关怀：\n   - 对自己保持善意\n   - 允许自己感受情绪\n   - 给自己时间恢复\n\n记住，情绪调节是一个技能，需要练习才能掌握。', '心理排忧', '自我调节', '情绪调节,自我调节,情绪管理,心理健康', NULL, 1, 0, '2026-01-16 17:21:51', '2026-01-16 17:21:51', NULL);
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (9, '了解自己的情绪模式', '了解自己的情绪模式是自我认知的重要部分。以下是一些方法帮助你更好地了解自己：\n\n1. 情绪日记：\n   - 记录每天的情绪变化\n   - 注意什么触发了特定情绪\n   - 观察情绪如何影响你的行为\n\n2. 反思练习：\n   - 定期反思自己的反应\n   - 思考为什么会有这样的情绪\n   - 识别情绪模式\n\n3. 寻求反馈：\n   - 询问信任的人对你的观察\n   - 接受建设性的反馈\n   - 从不同角度了解自己\n\n4. 心理测试：\n   - 进行性格测试（如MBTI、大五人格）\n   - 了解自己的情绪智力\n   - 认识自己的优势\n\n5. 观察身体反应：\n   - 注意情绪在身体上的表现\n   - 识别压力信号\n   - 学会倾听身体\n\n6. 接受复杂性：\n   - 认识到人是复杂的\n   - 允许自己有矛盾的情绪\n   - 不要试图简化自己\n\n了解自己是一个持续的过程，保持开放和好奇的态度。', '成长指南', '自我认知', '自我认知,情绪模式,个人成长,心理健康', NULL, 1, 0, '2026-01-16 17:21:51', '2026-01-16 17:21:51', NULL);
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (10, '建立健康的人际关系', '健康的人际关系对心理健康至关重要。以下是一些建立和维护健康关系的方法：\n\n1. 有效沟通：\n   - 清晰表达自己的需求和感受\n   - 积极倾听他人\n   - 使用\"我\"陈述而非指责\n\n2. 设定边界：\n   - 知道自己的界限\n   - 学会说\"不\"\n   - 尊重他人的边界\n\n3. 表达感激：\n   - 定期表达对他人感激\n   - 认可他人的贡献\n   - 保持积极的态度\n\n4. 解决冲突：\n   - 以尊重的方式处理分歧\n   - 寻求双赢的解决方案\n   - 愿意道歉和原谅\n\n5. 保持独立：\n   - 保持自己的兴趣和身份\n   - 不要过度依赖他人\n   - 尊重彼此的独立性\n\n6. 建立信任：\n   - 诚实和透明\n   - 遵守承诺\n   - 尊重隐私\n\n7. 投入时间：\n   - 定期与重要的人联系\n   - 创造共同回忆\n   - 在困难时互相支持\n\n记住，健康的关系需要双方的努力和投入。', '成长指南', '人际关系', '人际关系,沟通,社交,心理健康', NULL, 1, 0, '2026-01-16 17:21:51', '2026-01-16 17:21:51', NULL);
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (11, '从焦虑到平静：小明的故事', '小明是一名大学生，最近因为期末考试和未来就业的压力，开始出现严重的焦虑症状。他经常失眠，注意力难以集中，甚至出现了心悸和头痛。\n\n在朋友的鼓励下，小明决定寻求帮助。他首先咨询了学校的心理咨询师，学习了深呼吸和正念冥想技巧。同时，他开始规律作息，每天进行30分钟的散步。\n\n通过认知行为疗法，小明学会了识别和挑战自己的负面思维。他发现自己的焦虑往往源于对未来的过度担忧。他开始专注于当下，制定实际可行的计划。\n\n经过3个月的努力，小明的焦虑症状明显改善。他学会了更好地管理压力，睡眠质量也提高了。更重要的是，他建立了更健康的应对机制。\n\n小明的经历告诉我们，面对心理健康问题，寻求帮助是勇敢的表现。通过专业指导和自己的努力，是可以改善和恢复的。', '案例分享', NULL, '案例,焦虑,康复,心理健康', NULL, 1, 0, '2026-01-16 17:21:51', '2026-01-16 17:21:51', NULL);
INSERT INTO `knowledge` (`id`, `title`, `content`, `category`, `subcategory`, `tags`, `vector_id`, `status`, `view_count`, `create_time`, `update_time`, `create_by`) VALUES (12, '走出抑郁阴霾：小红的康复之路', '小红是一名职场女性，工作压力大，加上家庭关系紧张，逐渐陷入了抑郁状态。她感到持续的悲伤，对工作失去兴趣，甚至开始怀疑生活的意义。\n\n在家人和同事的关心下，小红意识到自己需要帮助。她联系了专业的心理咨询师，开始了治疗之旅。\n\n治疗过程中，小红学会了：\n1. 识别和表达自己的情绪\n2. 建立健康的生活习惯\n3. 改善人际关系\n4. 重新发现生活的意义\n\n同时，她开始服用抗抑郁药物，在医生的指导下逐渐调整。她还加入了支持小组，与其他有类似经历的人分享和交流。\n\n经过6个月的治疗，小红的情况明显好转。她重新找到了工作的热情，与家人的关系也改善了。她学会了更好地照顾自己，建立了支持网络。\n\n小红的经历说明，抑郁症是可以治疗的。关键是要寻求专业帮助，保持耐心，相信自己能够恢复。', '案例分享', NULL, '案例,抑郁,康复,治疗,心理健康', NULL, 1, 0, '2026-01-16 17:21:51', '2026-01-16 17:21:51', NULL);
COMMIT;

-- ----------------------------
-- Table structure for mystery_box_items
-- ----------------------------
DROP TABLE IF EXISTS `mystery_box_items`;
CREATE TABLE `mystery_box_items` (
  `box_item_id` bigint NOT NULL AUTO_INCREMENT,
  `content_type` enum('quote','task','tip') NOT NULL,
  `content` text NOT NULL,
  PRIMARY KEY (`box_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=123 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of mystery_box_items
-- ----------------------------
BEGIN;
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (83, 'quote', '今天的你比昨天更棒！');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (84, 'quote', '每一个微笑都是对生活的热爱');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (85, 'quote', '相信自己，你比想象中更强大');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (86, 'quote', '慢慢来，一切都会好起来的');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (87, 'quote', '今天的阳光正好，心情也要美美的');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (88, 'quote', '你是独一无二的，值得被温柔对待');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (89, 'quote', '每一个努力的日子都不会被辜负');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (90, 'quote', '保持微笑，生活会更美好');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (91, 'quote', '今天的你依然闪闪发光');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (92, 'quote', '相信自己，你正在成为更好的自己');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (93, 'task', '今天尝试做一件一直想做但没做的小事');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (94, 'task', '给一个朋友发个温暖的问候');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (95, 'task', '整理一下自己的房间或工作台');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (96, 'task', '尝试一种新的食物或饮料');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (97, 'task', '拍一张今天最美的照片');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (98, 'task', '给家人打个电话或发个消息');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (99, 'task', '学习一个新的小技能');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (100, 'task', '写下一件今天感恩的事情');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (101, 'task', '给陌生人一个微笑');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (102, 'task', '尝试一个新的运动或活动');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (103, 'task', '听一首从未听过的音乐');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (104, 'task', '画一幅简单的画');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (105, 'task', '给植物浇水或照顾宠物');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (106, 'task', '尝试一个新的发型或穿搭');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (107, 'task', '给未来的自己写一封信');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (108, 'tip', '深呼吸三次，感受当下的平静');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (109, 'tip', '写下三件今天让你感恩的事情');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (110, 'tip', '听一首喜欢的音乐，放松心情');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (111, 'tip', '给自己一个温暖的拥抱');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (112, 'tip', '闭上眼睛，想象美好的画面');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (113, 'tip', '做5分钟的简单拉伸运动');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (114, 'tip', '喝一杯温水，感受温暖');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (115, 'tip', '数数周围的五种颜色');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (116, 'tip', '深呼吸，感受空气的流动');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (117, 'tip', '给自己一个鼓励的话语');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (118, 'tip', '想象自己在一个安全的地方');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (119, 'tip', '做几个简单的瑜伽动作');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (120, 'tip', '听一段自然的声音');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (121, 'tip', '写下今天的三个小成就');
INSERT INTO `mystery_box_items` (`box_item_id`, `content_type`, `content`) VALUES (122, 'tip', '给自己一个微笑，即使心情不好');
COMMIT;

-- ----------------------------
-- Table structure for post_comments
-- ----------------------------
DROP TABLE IF EXISTS `post_comments`;
CREATE TABLE `post_comments` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` text NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `post_id` (`post_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `post_comments_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `anonymous_posts` (`post_id`) ON DELETE CASCADE,
  CONSTRAINT `post_comments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of post_comments
-- ----------------------------
BEGIN;
INSERT INTO `post_comments` (`comment_id`, `post_id`, `user_id`, `content`, `create_time`) VALUES (2, 3, 99999, '1', '2025-07-26 14:39:40');
INSERT INTO `post_comments` (`comment_id`, `post_id`, `user_id`, `content`, `create_time`) VALUES (3, 4, 99999, '1', '2025-07-26 15:00:57');
INSERT INTO `post_comments` (`comment_id`, `post_id`, `user_id`, `content`, `create_time`) VALUES (4, 3, 99999, '2', '2025-07-26 15:01:06');
INSERT INTO `post_comments` (`comment_id`, `post_id`, `user_id`, `content`, `create_time`) VALUES (5, 4, 99999, '2', '2025-07-26 15:29:53');
INSERT INTO `post_comments` (`comment_id`, `post_id`, `user_id`, `content`, `create_time`) VALUES (6, 6, 99999, '1', '2025-07-26 23:51:03');
INSERT INTO `post_comments` (`comment_id`, `post_id`, `user_id`, `content`, `create_time`) VALUES (7, 6, 1, '2', '2025-07-29 02:50:18');
INSERT INTO `post_comments` (`comment_id`, `post_id`, `user_id`, `content`, `create_time`) VALUES (8, 4, 1, '3', '2025-07-29 02:50:26');
INSERT INTO `post_comments` (`comment_id`, `post_id`, `user_id`, `content`, `create_time`) VALUES (9, 8, 1, '2', '2025-07-29 14:33:46');
INSERT INTO `post_comments` (`comment_id`, `post_id`, `user_id`, `content`, `create_time`) VALUES (10, 8, 1, '3', '2025-07-29 15:07:50');
INSERT INTO `post_comments` (`comment_id`, `post_id`, `user_id`, `content`, `create_time`) VALUES (11, 12, 1, '已接收', '2025-08-14 14:09:09');
COMMIT;

-- ----------------------------
-- Table structure for post_likes
-- ----------------------------
DROP TABLE IF EXISTS `post_likes`;
CREATE TABLE `post_likes` (
  `like_id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`like_id`),
  UNIQUE KEY `post_id_user_id` (`post_id`,`user_id`),
  KEY `post_likes_ibfk_2` (`user_id`),
  CONSTRAINT `post_likes_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `anonymous_posts` (`post_id`) ON DELETE CASCADE,
  CONSTRAINT `post_likes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of post_likes
-- ----------------------------
BEGIN;
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (4, 3, 99999);
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (6, 3, 100001);
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (14, 4, 1);
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (3, 4, 99999);
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (5, 4, 100000);
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (7, 4, 100001);
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (16, 7, 1);
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (19, 7, 100001);
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (17, 8, 1);
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (18, 8, 100001);
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (20, 11, 1);
INSERT INTO `post_likes` (`like_id`, `post_id`, `user_id`) VALUES (21, 12, 1);
COMMIT;

-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `tag_id` bigint NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(50) NOT NULL,
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `uk_tags_tag_name` (`tag_name`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of tags
-- ----------------------------
BEGIN;
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (12, '兴奋');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (23, '压力');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (19, '困惑');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (17, '失望');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (22, '孤独');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (25, '希望');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (15, '平静');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (11, '开心');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (26, '悲伤');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (21, '感激');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (16, '愤怒');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (24, '放松');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (20, '期待');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (18, '满足');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (14, '焦虑');
INSERT INTO `tags` (`tag_id`, `tag_name`) VALUES (13, '难过');
COMMIT;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `uid` bigint NOT NULL AUTO_INCREMENT,
  `account` varchar(64) NOT NULL,
  `password` varchar(100) DEFAULT NULL COMMENT '密码（邮箱登录用户可为空）',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint DEFAULT '0' COMMENT '状态 0正常 1禁用',
  `isdelete` tinyint DEFAULT '0' COMMENT '是否删除 0未删除 1已删除',
  `wx_openid` varchar(64) DEFAULT NULL COMMENT '微信openid',
  `qq_openid` varchar(64) DEFAULT NULL COMMENT 'QQ openid',
  `energy` int DEFAULT '0',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100019 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (1, 'test', '$2a$10$x8bdEIeR0SQdBIIjPCzytuN7YOuPtMShvWhjnKdmzKo6NjOQW2iSu', '13111111112', '2464344891@qq.com', 0, 0, NULL, NULL, 11, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (3, 'zhangsan', '$2a$10$27X.Fkzsilyqkwk3OPmubO2zam6mVezSNjn8DKkZ22E3MsY/8nOpu', '13111111111', '123@123.com', 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (4, 'zhang3', '$2a$10$gLaqZM4ONLpCMeT5edzvV.2IcDAzaGlXNC0aUr9moOhxrznzv6kw2', '13111111111', '3@163.com', 0, 1, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (99999, 'anonymous', 'anonymous', NULL, NULL, 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100000, 'lisi', '$2a$10$DS235Px226MoVShN8bQWZOaul2CQWu5wwf3ps4WEV8nssoxcA/b2m', NULL, NULL, 0, 0, NULL, NULL, 7, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100001, 'wangwu', '$2a$10$1P5/AGeBcctbwy.ddeh1uOCm1RJH4LXU.eBbvYVmyaing0.rWYaqa', NULL, NULL, 0, 0, NULL, NULL, 5, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100002, 'zhao6', '$2a$10$tQuQE3oSbgCP4PC3mtOw0.nLrHGl.QEpEiEwf45khBuKJZo4EdQFK', NULL, NULL, 0, 0, NULL, NULL, 6, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100003, 'ceshi', '$2a$10$agz4uBVrZt54QC6cfj3YguJaNwX3K0fzm6qa0YglW9NtP890IXt96', NULL, NULL, 0, 0, NULL, NULL, 5, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100004, 'wechat_928731', 'wechat_user', '', '', 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100005, 'wechat_65707', 'wechat_user', '', '', 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100006, 'wechat_72203', 'wechat_user', '', '', 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100007, 'wechat_user', 'wechat_user', '', '', 0, 0, NULL, NULL, 3, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100008, 'wx_oyOtt5e2JyzQYs_pF_ztDk8p3_s8', '$2a$10$a5dDx5OfHdwdp2qLJSu/t.LMzJB6la1byqcvmHhKnqs6SxK3g69Lu', NULL, NULL, 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100009, 'wx_oyOtt5e2JyzQYs_pF_ztDk8p3_s8', '$2a$10$BvaBm9yJp2PqKKAgaCu4NeVs0vfVCFjA0sRjRb5Ak8fCYPHI6JlD.', NULL, NULL, 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100010, 'wx_oyOtt5e2JyzQYs_pF_ztDk8p3_s8', '$2a$10$h8z3lL6iuJ0SnTSnkJ.iWu7yzxYZYR4YeK5/HQQBWtYS6qKXzc5yS', NULL, NULL, 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100011, 'wx_oyOtt5e2JyzQYs_pF_ztDk8p3_s8', '$2a$10$Y3qJtvbXs4aJPp4ULl4Q5eklJ0do4OK8Y.Kwo142KlvcGLGblQBS2', NULL, NULL, 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100012, 'wx_oyOtt5e2JyzQYs_pF_ztDk8p3_s8', '$2a$10$x4j1euIr5cdAs9jJh56zzOQvQ4fJA2/f5c6sWPUmru.nRUy.Kyzqa', NULL, NULL, 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100013, 'wx_oyOtt5e2JyzQYs_pF_ztDk8p3_s8', '$2a$10$4e0VqPhvsL9DzKftTiG6JOA0rrPxwSGKwxpjCAeEiYvzxSOZSrvB.', NULL, NULL, 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100014, 'wx_oyOtt5e2JyzQYs_pF_ztDk8p3_s8', '$2a$10$UQi9khhd8ZQNSFZuVCX/c.NEtUhKDhdbytjvtvedMs4GmKB/O3/5q', NULL, NULL, 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100015, 'wx_oyOtt5e2JyzQYs_pF_ztDk8p3_s8', '$2a$10$SycfJKTH8iu51nppw3tph.smP8uk0OkAL20TShs1956JBf7Kp65FS', NULL, NULL, 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100016, 'wx_oyOtt5e2JyzQYs_pF_ztDk8p3_s8', '$2a$10$gEvgH1ZVUlS4gX2BHcZhLusetwhoYbTJ.GJXLEdjcsXCYZuEIRaMS', NULL, NULL, 0, 0, NULL, NULL, 0, NULL);
INSERT INTO `user` (`uid`, `account`, `password`, `phone`, `email`, `status`, `isdelete`, `wx_openid`, `qq_openid`, `energy`, `avatar`) VALUES (100018, 'email_1747842680_1768585127021', NULL, NULL, '1747842680@qq.com', 0, 0, NULL, NULL, 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for user_daily_login_reward
-- ----------------------------
DROP TABLE IF EXISTS `user_daily_login_reward`;
CREATE TABLE `user_daily_login_reward` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `reward_date` date NOT NULL,
  `energy_reward` int NOT NULL DEFAULT '3',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`reward_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_reward_date` (`reward_date`),
  CONSTRAINT `fk_daily_login_reward_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of user_daily_login_reward
-- ----------------------------
BEGIN;
INSERT INTO `user_daily_login_reward` (`id`, `user_id`, `reward_date`, `energy_reward`, `create_time`) VALUES (1, 1, '2025-08-21', 3, '2025-08-21 13:51:51');
INSERT INTO `user_daily_login_reward` (`id`, `user_id`, `reward_date`, `energy_reward`, `create_time`) VALUES (2, 1, '2025-08-22', 3, '2025-08-22 00:34:15');
INSERT INTO `user_daily_login_reward` (`id`, `user_id`, `reward_date`, `energy_reward`, `create_time`) VALUES (3, 1, '2025-08-23', 3, '2025-08-23 16:30:43');
INSERT INTO `user_daily_login_reward` (`id`, `user_id`, `reward_date`, `energy_reward`, `create_time`) VALUES (4, 1, '2025-08-26', 3, '2025-08-26 14:46:11');
INSERT INTO `user_daily_login_reward` (`id`, `user_id`, `reward_date`, `energy_reward`, `create_time`) VALUES (5, 1, '2025-08-27', 3, '2025-08-27 00:03:03');
INSERT INTO `user_daily_login_reward` (`id`, `user_id`, `reward_date`, `energy_reward`, `create_time`) VALUES (6, 1, '2025-08-29', 3, '2025-08-29 13:14:50');
INSERT INTO `user_daily_login_reward` (`id`, `user_id`, `reward_date`, `energy_reward`, `create_time`) VALUES (7, 1, '2026-01-12', 3, '2026-01-12 18:52:36');
COMMIT;

-- ----------------------------
-- Table structure for user_daily_quote
-- ----------------------------
DROP TABLE IF EXISTS `user_daily_quote`;
CREATE TABLE `user_daily_quote` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `content` varchar(500) NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_daily_quote_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`uid`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of user_daily_quote
-- ----------------------------
BEGIN;
INSERT INTO `user_daily_quote` (`id`, `user_id`, `content`, `create_time`, `update_time`) VALUES (1, 100000, '每一个微笑都是对生活的热爱', '2025-07-26 15:52:10', '2025-07-27 16:12:03');
INSERT INTO `user_daily_quote` (`id`, `user_id`, `content`, `create_time`, `update_time`) VALUES (2, 1, '相信自己，你比想象中更强大', '2025-07-26 18:58:02', '2025-08-22 00:37:25');
INSERT INTO `user_daily_quote` (`id`, `user_id`, `content`, `create_time`, `update_time`) VALUES (3, 100002, '你正在成为更好的自己', '2025-07-27 16:53:15', '2025-07-27 16:53:15');
INSERT INTO `user_daily_quote` (`id`, `user_id`, `content`, `create_time`, `update_time`) VALUES (4, 100001, '每一个今天都是新的开始', '2025-07-27 17:02:30', '2025-07-29 15:13:58');
COMMIT;

-- ----------------------------
-- Table structure for user_drawn_boxes
-- ----------------------------
DROP TABLE IF EXISTS `user_drawn_boxes`;
CREATE TABLE `user_drawn_boxes` (
  `drawn_box_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `box_item_id` bigint NOT NULL,
  `draw_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_completed` tinyint(1) DEFAULT '0' COMMENT '0 for not completed/not applicable, 1 for completed',
  PRIMARY KEY (`drawn_box_id`),
  KEY `user_id` (`user_id`),
  KEY `box_item_id` (`box_item_id`),
  CONSTRAINT `user_drawn_boxes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`uid`) ON DELETE CASCADE,
  CONSTRAINT `user_drawn_boxes_ibfk_2` FOREIGN KEY (`box_item_id`) REFERENCES `mystery_box_items` (`box_item_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of user_drawn_boxes
-- ----------------------------
BEGIN;
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (28, 100001, 94, '2025-07-27 18:31:44', 1);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (29, 1, 112, '2025-07-27 19:06:36', 0);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (30, 1, 107, '2025-07-27 19:06:45', 1);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (31, 1, 94, '2025-07-29 13:39:17', 1);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (32, 100001, 91, '2025-07-29 15:18:43', 0);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (33, 100001, 116, '2025-07-29 15:18:51', 0);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (34, 1, 93, '2025-07-29 15:24:37', 0);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (35, 1, 106, '2025-07-29 15:27:23', 1);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (36, 1, 89, '2025-07-29 15:32:13', 0);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (37, 1, 105, '2025-07-29 15:34:55', 1);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (38, 1, 85, '2025-08-01 15:35:42', 0);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (39, 1, 114, '2025-08-07 17:33:52', 0);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (40, 1, 99, '2025-08-07 17:33:58', 1);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (41, 100000, 108, '2025-08-08 16:12:02', 0);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (42, 100000, 110, '2025-08-08 16:12:07', 0);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (43, 100000, 119, '2025-08-08 16:12:13', 0);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (44, 100000, 89, '2025-08-08 16:12:18', 0);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (45, 1, 105, '2025-08-11 22:18:12', 1);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (46, 1, 107, '2026-01-15 01:58:51', 1);
INSERT INTO `user_drawn_boxes` (`drawn_box_id`, `user_id`, `box_item_id`, `draw_time`, `is_completed`) VALUES (47, 1, 106, '2026-01-16 02:46:46', 0);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

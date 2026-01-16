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

 Date: 17/01/2026 03:21:45
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

SET FOREIGN_KEY_CHECKS = 1;

/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 100432
 Source Host           : localhost:3306
 Source Schema         : todo

 Target Server Type    : MySQL
 Target Server Version : 100432
 File Encoding         : 65001

 Date: 26/08/2025 12:00:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for todos
-- ----------------------------
DROP TABLE IF EXISTS `todos`;
CREATE TABLE `todos`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `completed` tinyint(1) NOT NULL DEFAULT 0,
  `created` timestamp(0) NOT NULL DEFAULT current_timestamp(),
  `updated` timestamp(0) NOT NULL DEFAULT current_timestamp(),
  `deleted` int(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of todos
-- ----------------------------
INSERT INTO `todos` VALUES (2, 'Новая задача', 1, '2025-08-19 14:32:21', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (3, 'Новая задача', 1, '2025-08-19 14:32:37', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (4, 'Новая задача', 1, '2025-08-19 14:40:30', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (5, 'Новая задача', 1, '2025-08-19 15:09:42', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (6, 'Новая задача', 1, '2025-08-19 15:11:35', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (7, 'Новая задача', 1, '2025-08-20 12:35:16', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (8, 'Новая задача', 1, '2025-08-20 12:40:35', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (9, 'Новая задача', 1, '2025-08-20 12:45:36', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (10, 'Новая задача', 1, '2025-08-20 12:52:34', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (11, 'Новая задача', 1, '2025-08-20 13:02:01', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (12, 'Новая задача', 1, '2025-08-21 10:51:05', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (13, 'Новая задача', 1, '2025-08-21 11:00:11', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (14, 'Новая задача', 1, '2025-08-21 11:01:12', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (15, 'Новая задача', 1, '2025-08-21 11:01:33', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (16, 'Новая задача', 1, '2025-08-21 11:01:57', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (17, 'Обновленная задача', 1, '2025-08-21 15:09:22', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (18, 'Новая задача', 1, '2025-08-21 15:07:21', '2025-08-25 12:30:15', 1);
INSERT INTO `todos` VALUES (19, 'Новая задача', 1, '2025-08-21 15:09:00', '2025-08-26 11:40:44', 1);
INSERT INTO `todos` VALUES (20, 'Обновленная задача', 1, '2025-08-21 15:43:52', '2025-08-26 11:37:58', 1);
INSERT INTO `todos` VALUES (21, 'Обновленная задача', 0, '2025-08-25 17:50:45', '2025-08-25 17:53:02', 0);

SET FOREIGN_KEY_CHECKS = 1;

CREATE DATABASE IF NOT EXISTS `pet_adoption`;
USE `pet_adoption`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `pet_infomations`;

CREATE TABLE `users` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `username` varchar(255) NOT NULL UNIQUE,
    `password` varchar(255) NOT NULL,
    `email` varchar(255) NOT NULL UNIQUE,
    `role` tinyint NOT NULL,
    `avatar` varchar(255),
    `createTime` datetime NOT NULL,
    `updateTime` datetime NOT NULL,
    
    PRIMARY KEY (`id`)
) COMMENT '用户表';

CREATE TABLE `pet_infomations` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `gender` tinyint NOT NULL,
    `age` tinyint NOT NULL,
    `breed` varchar(255) NOT NULL,
    `description` varchar(255) NOT NULL,
    `image` varchar(255) NOT NULL,
    `status` tinyint NOT NULL,
    `createTime` datetime NOT NULL,

    PRIMARY KEY (`id`)
) COMMENT '流浪宠物信息表';

SET FOREIGN_KEY_CHECKS = 1;
CREATE DATABASE IF NOT EXISTS `pet_adoption` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pet_adoption`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `pet_video`;
DROP TABLE IF EXISTS `pet_image`;
DROP TABLE IF EXISTS `pet_location`;
DROP TABLE IF EXISTS `pet_information`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 id',
    `username` VARCHAR(255) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `email` VARCHAR(255) NOT NULL COMMENT '邮箱',
    `role` INT NOT NULL COMMENT '角色',
    `avatar` VARCHAR(255) COMMENT '头像',
    `createTime` DATE NOT NULL COMMENT '创建时间',
    `updateTime` DATE NOT NULL COMMENT '最后一次修改时间',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_email` (`email`)
) COMMENT='用户账户信息';

CREATE TABLE `pet_information` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '宠物 id',
    `userId` BIGINT NOT NULL COMMENT '发现该宠物的用户 id',
    `name` VARCHAR(255) DEFAULT NULL COMMENT '宠物名称',
    `minAge` INT DEFAULT NULL COMMENT '宠物最小年龄',
    `maxAge` INT DEFAULT NULL COMMENT '宠物最大年龄',
    `sex` VARCHAR(10) NOT NULL COMMENT '宠物性别',
    `type` VARCHAR(32) NOT NULL COMMENT '宠物类型',
    `breed` VARCHAR(32) DEFAULT NULL COMMENT '宠物品种',
    `health` VARCHAR(10) DEFAULT NULL COMMENT '宠物健康情况',
    `vaccine` VARCHAR(10) DEFAULT NULL COMMENT '宠物疫苗情况',
    `description` TEXT COMMENT '宠物描述',
    `status` INT NOT NULL COMMENT '宠物发现状态',
    `createTime` DATETIME NOT NULL COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL COMMENT '最后一次修改时间',

    PRIMARY KEY (`id`),
    CONSTRAINT `fk_pet_information_userId` FOREIGN KEY (`userId`) REFERENCES `user` (`id`)
) COMMENT='流浪宠物信息';

CREATE TABLE `pet_location` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `petId` BIGINT NOT NULL COMMENT '宠物 id',
    'userId' BIGINT NOT NULL COMMENT '用户 id',
    `province` VARCHAR(10) NOT NULL COMMENT '发现省份',
    `city` VARCHAR(10) NOT NULL COMMENT '发现城市',
    `county` VARCHAR(64) NOT NULL COMMENT '发现县/县级市',
    `detailAddress` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `date` DATE NOT NULL COMMENT '发现时间',
    
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_pet_location_petId` FOREIGN KEY (`petId`) REFERENCES `pet_information` (`id`),
    CONSTRAINT `fk_pet_location_userId` FOREIGN KEY (`userId`) REFERENCES `user` (`id`)
) COMMENT='发现流浪宠物的位置';

CREATE TABLE `pet_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `petId` BIGINT NOT NULL COMMENT '宠物 id',
    `name` VARCHAR(255) DEFAULT NULL,
    `filename` VARCHAR(255) DEFAULT NULL,
    `description` TEXT,
    `isCover` TINYINT(1) DEFAULT NULL,

    PRIMARY KEY (`id`),
    CONSTRAINT `fk_pet_image_petId` FOREIGN KEY (`petId`) REFERENCES `pet_information` (`id`)
) COMMENT='宠物图片';

CREATE TABLE `pet_video` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `petId` BIGINT NOT NULL COMMENT '宠物 id',
    `name` VARCHAR(255) DEFAULT NULL,
    `filename` VARCHAR(255) DEFAULT NULL,
    `description` TEXT,

    PRIMARY KEY (`id`),
    CONSTRAINT `fk_pet_video_petId` FOREIGN KEY (`petId`) REFERENCES `pet_information` (`id`)
) COMMENT='宠物视频';

SET FOREIGN_KEY_CHECKS = 1;

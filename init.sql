CREATE DATABASE IF NOT EXISTS `pet_adoption` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pet_adoption`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `delete_job`;
DROP TABLE IF EXISTS `article_favorite`;
DROP TABLE IF EXISTS `article_like`;
DROP TABLE IF EXISTS `article`;
DROP TABLE IF EXISTS `volunteer_reward`;
DROP TABLE IF EXISTS `volunteer_service_record`;
DROP TABLE IF EXISTS `volunteer_shift_status_record`;
DROP TABLE IF EXISTS `volunteer_shift`;
DROP TABLE IF EXISTS `volunteer_task`;
DROP TABLE IF EXISTS `volunteer_profile`;
DROP TABLE IF EXISTS `volunteer_application`;
DROP TABLE IF EXISTS `volunteer_recruitment`;
DROP TABLE IF EXISTS `lost_pet_mismatch`;
DROP TABLE IF EXISTS `lost_pet_claim`;
DROP TABLE IF EXISTS `lost_pet`;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS `item`;
DROP TABLE IF EXISTS `subscribe`;
DROP TABLE IF EXISTS `stock_record`;
DROP TABLE IF EXISTS `stock`;
DROP TABLE IF EXISTS `donation_status_update_record`;
DROP TABLE IF EXISTS `donation_item`;
DROP TABLE IF EXISTS `donation_file`;
DROP TABLE IF EXISTS `donation`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `follow_record`;
DROP TABLE IF EXISTS `follow_task`;
DROP TABLE IF EXISTS `agreement_update_record`;
DROP TABLE IF EXISTS `agreement_file`;
DROP TABLE IF EXISTS `agreement`;
DROP TABLE IF EXISTS `breading`;
DROP TABLE IF EXISTS `adopt`;
DROP TABLE IF EXISTS `health_assessment`;
DROP TABLE IF EXISTS `rehab_record`;
DROP TABLE IF EXISTS `rehab_plan_status`;
DROP TABLE IF EXISTS `rehab_plan`;
DROP TABLE IF EXISTS `immunity_history`;
DROP TABLE IF EXISTS `allergy_history`;
DROP TABLE IF EXISTS `deworm_record`;
DROP TABLE IF EXISTS `dewormer`;
DROP TABLE IF EXISTS `vaccine_record`;
DROP TABLE IF EXISTS `vaccine`;
DROP TABLE IF EXISTS `examination_file`;
DROP TABLE IF EXISTS `examination_diagnosis`;
DROP TABLE IF EXISTS `examination`;
DROP TABLE IF EXISTS `treatment_plan`;
DROP TABLE IF EXISTS `diagnosis`;
DROP TABLE IF EXISTS `medical_detail`;
DROP TABLE IF EXISTS `medical_record`;
DROP TABLE IF EXISTS `first_registration`;
DROP TABLE IF EXISTS `rescue_task_record`;
DROP TABLE IF EXISTS `rescue_task_assign`;
DROP TABLE IF EXISTS `rescue_task`;
DROP TABLE IF EXISTS `pet_status_record`;
DROP TABLE IF EXISTS `pet_tag`;
DROP TABLE IF EXISTS `media_file`;
DROP TABLE IF EXISTS `location`;
DROP TABLE IF EXISTS `pet`;
DROP TABLE IF EXISTS `notice`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户账户信息',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `email` varchar(255) NOT NULL COMMENT '邮箱',
  `role` tinyint NOT NULL COMMENT '角色',
  `avatar` varchar(255) COMMENT '头像',
  `phone` varchar(50) COMMENT '联系方式',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后一次修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账户信息';

CREATE TABLE `notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '站内信',
  `receiver_id` bigint NOT NULL COMMENT '接收用户',
  `source` varchar(20) NOT NULL COMMENT '消息来源',
  `title` varchar(255) NOT NULL COMMENT '消息标题',
  `content` text NOT NULL COMMENT '消息正文',
  `read` tinyint NOT NULL COMMENT '是否已读',
  `read_time` datetime COMMENT '已读时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_notice_receiver_id` (`receiver_id`),
  CONSTRAINT `fk_notice_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内信';

CREATE TABLE `pet` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '流浪宠物信息',
  `discover_id` bigint NOT NULL COMMENT '发现该宠物的用户 id',
  `name` varchar(50) COMMENT '宠物名称',
  `age` int COMMENT '宠物年龄（月）',
  `sex` varchar(10) COMMENT '宠物性别',
  `type` varchar(50) COMMENT '宠物类型',
  `breed` varchar(50) COMMENT '宠物品种',
  `health` varchar(255) COMMENT '宠物健康情况',
  `description` text COMMENT '宠物描述',
  `status` varchar(20) NOT NULL COMMENT '宠物记录状态',
  `is_discard` tinyint NOT NULL COMMENT '是否被废弃',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后一次修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_discover_id` (`discover_id`),
  CONSTRAINT `fk_pet_discover_id` FOREIGN KEY (`discover_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流浪宠物信息';

CREATE TABLE `location` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '位置信息',
  `parent_id` bigint NOT NULL COMMENT '绑定类型 id',
  `parent_type` varchar(20) NOT NULL COMMENT '绑定类型',
  `user_id` bigint NOT NULL COMMENT '发现/记录者 id',
  `province` varchar(20) NOT NULL COMMENT '省份',
  `city` varchar(20) NOT NULL COMMENT '城市',
  `district` varchar(20) NOT NULL COMMENT '县/县级市',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_location_user_id` (`user_id`),
  CONSTRAINT `fk_location_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='位置信息';

CREATE TABLE `media_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '媒体信息（图片、视频）',
  `parent_id` bigint NOT NULL COMMENT '与之关联的资源 id',
  `parent_type` varchar(20) NOT NULL COMMENT '关联类型',
  `user_id` bigint NOT NULL COMMENT '上传用户 id',
  `name` varchar(255) NOT NULL COMMENT '资源名',
  `description` varchar(255) COMMENT '资源介绍',
  `is_cover` tinyint NOT NULL COMMENT '（图片）是否为封面',
  `filename` varchar(255) NOT NULL COMMENT '存储文件名',
  `type` varchar(20) NOT NULL COMMENT '类型',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_media_file_user_id` (`user_id`),
  CONSTRAINT `fk_media_file_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒体信息（图片、视频）';

CREATE TABLE `pet_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '宠物标签',
  `pet_id` bigint NOT NULL COMMENT '流浪宠物 id',
  `user_id` bigint NOT NULL COMMENT '添加用户 id',
  `tag` varchar(20) NOT NULL COMMENT '标签内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_tag_pet_id` (`pet_id`),
  CONSTRAINT `fk_pet_tag_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  KEY `idx_pet_tag_user_id` (`user_id`),
  CONSTRAINT `fk_pet_tag_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物标签';

CREATE TABLE `pet_status_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '宠物状态变更记录',
  `pet_id` bigint NOT NULL COMMENT '流浪宠物 id',
  `user_id` bigint NOT NULL COMMENT '提交用户 id',
  `from` varchar(20) NOT NULL COMMENT '旧状态',
  `to` varchar(20) NOT NULL COMMENT '新状态',
  `description` text COMMENT '转移说明',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_pet_status_record_pet_id` (`pet_id`),
  CONSTRAINT `fk_pet_status_record_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  KEY `idx_pet_status_record_user_id` (`user_id`),
  CONSTRAINT `fk_pet_status_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物状态变更记录';

CREATE TABLE `rescue_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '救助任务',
  `previous_id` bigint COMMENT '上一个任务 id',
  `user_id` bigint NOT NULL COMMENT '创建者',
  `approve_id` bigint DEFAULT NULL COMMENT '审核者',
  `summary` varchar(20) NOT NULL COMMENT '简述',
  `description` text COMMENT '详细描述',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `type` varchar(20) NOT NULL COMMENT '类型',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后一次更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_rescue_task_previous_id` (`previous_id`),
  CONSTRAINT `fk_rescue_task_previous_id` FOREIGN KEY (`previous_id`) REFERENCES `rescue_task` (`id`),
  KEY `idx_rescue_task_user_id` (`user_id`),
  CONSTRAINT `fk_rescue_task_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  KEY `idx_rescue_task_approve_id` (`approve_id`),
  CONSTRAINT `fk_rescue_task_approve_id` FOREIGN KEY (`approve_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='救助任务';

CREATE TABLE `rescue_task_assign` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '救助任务分配信息',
  `task_id` bigint NOT NULL COMMENT '救助任务 id',
  `user_id` bigint NOT NULL COMMENT '任务执行者 id',
  `assigner_id` bigint NOT NULL COMMENT '任务分配者 id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rescue_task_assign_task_id` (`task_id`),
  CONSTRAINT `fk_rescue_task_assign_task_id` FOREIGN KEY (`task_id`) REFERENCES `rescue_task` (`id`),
  KEY `idx_rescue_task_assign_user_id` (`user_id`),
  CONSTRAINT `fk_rescue_task_assign_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  KEY `idx_rescue_task_assign_assigner_id` (`assigner_id`),
  CONSTRAINT `fk_rescue_task_assign_assigner_id` FOREIGN KEY (`assigner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='救助任务分配信息';

CREATE TABLE `rescue_task_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '救助任务状态记录',
  `task_id` bigint NOT NULL COMMENT '救助任务 id',
  `user_id` bigint NOT NULL COMMENT '发起者 id',
  `action` varchar(20) NOT NULL COMMENT '记录类型',
  `status_from` varchar(20) NOT NULL COMMENT '修改前的任务状态',
  `status_to` varchar(20) NOT NULL COMMENT '修改后的任务状态',
  `reason` text NOT NULL COMMENT '修改原因',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rescue_task_record_task_id` (`task_id`),
  CONSTRAINT `fk_rescue_task_record_task_id` FOREIGN KEY (`task_id`) REFERENCES `rescue_task` (`id`),
  KEY `idx_rescue_task_record_user_id` (`user_id`),
  CONSTRAINT `fk_rescue_task_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='救助任务状态记录';

CREATE TABLE `first_registration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '初诊登记',
  `registrar_id` bigint COMMENT '登记人 id',
  `pet_id` bigint COMMENT '宠物 id',
  `name` varchar(20) COMMENT '宠物名称',
  `age` int NOT NULL COMMENT '宠物年龄（月）',
  `weight` decimal(6,2) NOT NULL COMMENT '宠物体重 kg',
  `temperature` decimal(4,2) NOT NULL COMMENT '宠物体温 ℃',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_first_registration_registrar_id` (`registrar_id`),
  CONSTRAINT `fk_first_registration_registrar_id` FOREIGN KEY (`registrar_id`) REFERENCES `user` (`id`),
  KEY `idx_first_registration_pet_id` (`pet_id`),
  CONSTRAINT `fk_first_registration_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='初诊登记';

CREATE TABLE `medical_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '就诊记录',
  `pet_id` bigint NOT NULL COMMENT '流浪宠物 id',
  `pet_age` int NOT NULL COMMENT '流浪宠物年龄',
  `doctor_id` bigint NOT NULL COMMENT '接诊人',
  `owner_id` bigint COMMENT '领养人',
  `owner_phone` varchar(20) COMMENT '领养人联系方式',
  `status` varchar(20) NOT NULL COMMENT '诊疗流程',
  `type` varchar(20) NOT NULL COMMENT '诊疗类型',
  `start_time` datetime COMMENT '开始时间, null 表示未开始',
  `end_time` datetime COMMENT '结束时间',
  `price` decimal(10,2) NOT NULL COMMENT '预计价格',
  `cost` decimal(10,2) NOT NULL COMMENT '实际花费',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_medical_record_pet_id` (`pet_id`),
  CONSTRAINT `fk_medical_record_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  KEY `idx_medical_record_doctor_id` (`doctor_id`),
  CONSTRAINT `fk_medical_record_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`),
  KEY `idx_medical_record_owner_id` (`owner_id`),
  CONSTRAINT `fk_medical_record_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='就诊记录';

CREATE TABLE `medical_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '病历',
  `record_id` bigint NOT NULL COMMENT '就诊记录 id',
  `doctor_id` bigint NOT NULL COMMENT '兽医 id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  `is_completed` tinyint NOT NULL COMMENT '是否已完成',
  `is_discard` tinyint NOT NULL COMMENT '是否已废弃',
  `summary` varchar(255) COMMENT '摘要，用于显示在列表中',
  `description` text NOT NULL COMMENT '问题描述',
  `history` text NOT NULL COMMENT '现病史',
  `past_history` text NOT NULL COMMENT '既往史',
  `life_habit` text NOT NULL COMMENT '生活习性',
  `weight` decimal(6,2) NOT NULL COMMENT '宠物体重 kg',
  `temperature` decimal(4,2) NOT NULL COMMENT '宠物体温 ℃',
  `heart_rate` int NOT NULL COMMENT '心率',
  `respiratory_rate` int NOT NULL COMMENT '呼吸频率',
  `physical_exam` text NOT NULL COMMENT '其他体检信息',
  `diagnosis` text COMMENT '诊断结果',
  `differential` text COMMENT '鉴别诊断',
  `exam` text COMMENT '检查计划',
  `treatment` text COMMENT '治疗方案 概括',
  `advice` text COMMENT '医嘱',
  PRIMARY KEY (`id`),
  KEY `idx_medical_detail_record_id` (`record_id`),
  CONSTRAINT `fk_medical_detail_record_id` FOREIGN KEY (`record_id`) REFERENCES `medical_record` (`id`),
  KEY `idx_medical_detail_doctor_id` (`doctor_id`),
  CONSTRAINT `fk_medical_detail_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='病历';

CREATE TABLE `diagnosis` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '医疗检查结果',
  `detail_id` bigint NOT NULL COMMENT '病历 id',
  `result` varchar(255) NOT NULL COMMENT '检查结果',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `is_discard` tinyint NOT NULL COMMENT '已废弃',
  PRIMARY KEY (`id`),
  KEY `idx_diagnosis_detail_id` (`detail_id`),
  CONSTRAINT `fk_diagnosis_detail_id` FOREIGN KEY (`detail_id`) REFERENCES `medical_detail` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医疗检查结果';

CREATE TABLE `treatment_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '治疗计划',
  `doctor_id` bigint NOT NULL COMMENT '兽医 id',
  `detail_id` bigint COMMENT '病历 id',
  `plan` text NOT NULL COMMENT '治疗计划',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime COMMENT '结束时间，留空表示长期',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `is_discard` tinyint NOT NULL COMMENT '废弃状态',
  PRIMARY KEY (`id`),
  KEY `idx_treatment_plan_doctor_id` (`doctor_id`),
  CONSTRAINT `fk_treatment_plan_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`),
  KEY `idx_treatment_plan_detail_id` (`detail_id`),
  CONSTRAINT `fk_treatment_plan_detail_id` FOREIGN KEY (`detail_id`) REFERENCES `medical_detail` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗计划';

CREATE TABLE `examination` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '医疗检查记录',
  `user_id` bigint NOT NULL COMMENT '检查人',
  `detail_id` bigint NOT NULL COMMENT '绑定病历 id',
  `name` varchar(20) NOT NULL COMMENT '检查名',
  `text` text COMMENT '文本记录',
  `exam_type` varchar(20) NOT NULL COMMENT '医疗记录类型',
  `check_time` datetime NOT NULL COMMENT '检查时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_examination_user_id` (`user_id`),
  CONSTRAINT `fk_examination_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  KEY `idx_examination_detail_id` (`detail_id`),
  CONSTRAINT `fk_examination_detail_id` FOREIGN KEY (`detail_id`) REFERENCES `medical_detail` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医疗检查记录';

CREATE TABLE `examination_diagnosis` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '检查记录 - 客观诊断结果关联表',
  `examination_id` bigint NOT NULL COMMENT '医疗记录 id',
  `diagnosis_id` bigint NOT NULL COMMENT '诊断结果 id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_examination_diagnosis_examination_id` (`examination_id`),
  CONSTRAINT `fk_examination_diagnosis_examination_id` FOREIGN KEY (`examination_id`) REFERENCES `examination` (`id`),
  KEY `idx_examination_diagnosis_diagnosis_id` (`diagnosis_id`),
  CONSTRAINT `fk_examination_diagnosis_diagnosis_id` FOREIGN KEY (`diagnosis_id`) REFERENCES `diagnosis` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检查记录 - 客观诊断结果关联表';

CREATE TABLE `examination_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '检查文件',
  `examination_id` bigint NOT NULL COMMENT '检验 id',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `filename` varchar(255) NOT NULL COMMENT '文件名',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_examination_file_examination_id` (`examination_id`),
  CONSTRAINT `fk_examination_file_examination_id` FOREIGN KEY (`examination_id`) REFERENCES `examination` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检查文件';

CREATE TABLE `vaccine` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '疫苗信息',
  `item_id` bigint NOT NULL COMMENT '物品 id',
  `illness` varchar(255) NOT NULL COMMENT '疾病名称',
  `min_age` int NOT NULL COMMENT '接种最小年龄',
  `times` int NOT NULL COMMENT '总针数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_vaccine_item_id` (`item_id`),
  CONSTRAINT `fk_vaccine_item_id` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='疫苗信息';

CREATE TABLE `vaccine_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '疫苗接种记录',
  `pet_id` bigint NOT NULL COMMENT '宠物 id',
  `pet_age` int NOT NULL COMMENT '宠物年龄（月）',
  `doctor_id` bigint NOT NULL COMMENT '医生 id',
  `vaccine_id` bigint NOT NULL COMMENT '疫苗 id',
  `times` int NOT NULL COMMENT '针次',
  `create_time` date NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_vaccine_record_pet_id` (`pet_id`),
  CONSTRAINT `fk_vaccine_record_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  KEY `idx_vaccine_record_doctor_id` (`doctor_id`),
  CONSTRAINT `fk_vaccine_record_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`),
  KEY `idx_vaccine_record_vaccine_id` (`vaccine_id`),
  CONSTRAINT `fk_vaccine_record_vaccine_id` FOREIGN KEY (`vaccine_id`) REFERENCES `vaccine` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='疫苗接种记录';

CREATE TABLE `dewormer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '驱虫药',
  `item_id` bigint NOT NULL COMMENT '物品 id',
  `type` varchar(20) NOT NULL COMMENT '类型，内驱/外驱/其他',
  `min_age` int NOT NULL COMMENT '适用最小年龄',
  `times` int NOT NULL COMMENT '总次数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_dewormer_item_id` (`item_id`),
  CONSTRAINT `fk_dewormer_item_id` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='驱虫药';

CREATE TABLE `deworm_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '/',
  `pet_id` bigint NOT NULL COMMENT '宠物id',
  `doctor_id` bigint NOT NULL COMMENT '医生 id',
  `dewormer_id` bigint NOT NULL COMMENT '驱虫药 id',
  `times` int NOT NULL COMMENT '次数',
  `create_time` date NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_deworm_record_pet_id` (`pet_id`),
  CONSTRAINT `fk_deworm_record_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  KEY `idx_deworm_record_doctor_id` (`doctor_id`),
  CONSTRAINT `fk_deworm_record_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`),
  KEY `idx_deworm_record_dewormer_id` (`dewormer_id`),
  CONSTRAINT `fk_deworm_record_dewormer_id` FOREIGN KEY (`dewormer_id`) REFERENCES `dewormer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='DewormRecord';

CREATE TABLE `immunity_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '免疫史',
  `registration_id` bigint NOT NULL COMMENT '初诊登记 id',
  `medicine` varchar(255) NOT NULL COMMENT '免疫药品',
  `illness` varchar(255) NOT NULL COMMENT '免疫疾病',
  `count` int NOT NULL COMMENT '第几次免疫',
  `total` int NOT NULL COMMENT '总需要的免疫次数',
  `immunity_time` datetime NOT NULL COMMENT '免疫时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_immunity_history_registration_id` (`registration_id`),
  CONSTRAINT `fk_immunity_history_registration_id` FOREIGN KEY (`registration_id`) REFERENCES `first_registration` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='免疫史';

CREATE TABLE `allergy_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '过敏史',
  `registration_id` bigint NOT NULL COMMENT '初诊登记 id',
  `source` varchar(255) NOT NULL COMMENT '过敏源',
  `discovery_time` datetime NOT NULL COMMENT '发现时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_allergy_history_registration_id` (`registration_id`),
  CONSTRAINT `fk_allergy_history_registration_id` FOREIGN KEY (`registration_id`) REFERENCES `first_registration` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='过敏史';

CREATE TABLE `rehab_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '/',
  `doctor_id` bigint NOT NULL COMMENT '兽医 id',
  `pet_id` bigint NOT NULL COMMENT '流浪宠物 id',
  `pet_age` int NOT NULL COMMENT '流浪宠物年龄（月）',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `frequency` varchar(255) NOT NULL COMMENT '执行频率',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '预计结束时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rehab_plan_doctor_id` (`doctor_id`),
  CONSTRAINT `fk_rehab_plan_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `user` (`id`),
  KEY `idx_rehab_plan_pet_id` (`pet_id`),
  CONSTRAINT `fk_rehab_plan_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RehabPlan';

CREATE TABLE `rehab_plan_status` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '康复计划状态变更记录',
  `user_id` bigint NOT NULL COMMENT '提交用户 id',
  `plan_id` bigint NOT NULL COMMENT '康复计划 id',
  `status` varchar(20) NOT NULL COMMENT '新状态',
  `reason` varchar(255) NOT NULL COMMENT '变更原因',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rehab_plan_status_user_id` (`user_id`),
  CONSTRAINT `fk_rehab_plan_status_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  KEY `idx_rehab_plan_status_plan_id` (`plan_id`),
  CONSTRAINT `fk_rehab_plan_status_plan_id` FOREIGN KEY (`plan_id`) REFERENCES `rehab_plan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='康复计划状态变更记录';

CREATE TABLE `rehab_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '康复记录',
  `plan_id` bigint NOT NULL COMMENT '康复计划 id',
  `user_id` bigint NOT NULL COMMENT '执行人员 id',
  `step` varchar(255) NOT NULL COMMENT '执行步骤',
  `reaction` varchar(255) NOT NULL COMMENT '宠物反应',
  `note` varchar(255) NOT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rehab_record_plan_id` (`plan_id`),
  CONSTRAINT `fk_rehab_record_plan_id` FOREIGN KEY (`plan_id`) REFERENCES `rehab_plan` (`id`),
  KEY `idx_rehab_record_user_id` (`user_id`),
  CONSTRAINT `fk_rehab_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='康复记录';

CREATE TABLE `health_assessment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '健康评估',
  `pet_id` bigint NOT NULL COMMENT '宠物 id',
  `assessor_id` bigint NOT NULL COMMENT '评估者 id',
  `age` int NOT NULL COMMENT '宠物年龄',
  `weight` decimal(6,2) NOT NULL COMMENT '宠物体重',
  `score_bcs` int NOT NULL COMMENT '体况评分',
  `score_mental` int NOT NULL COMMENT '精神状态评分',
  `score_appetite` int NOT NULL COMMENT '食欲评分',
  `summary` varchar(255) NOT NULL COMMENT '评估结果',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_health_assessment_pet_id` (`pet_id`),
  CONSTRAINT `fk_health_assessment_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  KEY `idx_health_assessment_assessor_id` (`assessor_id`),
  CONSTRAINT `fk_health_assessment_assessor_id` FOREIGN KEY (`assessor_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康评估';

CREATE TABLE `adopt` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '领养申请',
  `pet_id` bigint NOT NULL COMMENT '宠物 id',
  `applicant_id` bigint NOT NULL COMMENT '申请人',
  `applicant_phone` varchar(20) NOT NULL COMMENT '申请人联系方式',
  `reviewer_id` bigint COMMENT '审核人',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `requirement` text NOT NULL COMMENT '申请要求',
  `reject_reason` text COMMENT '审核拒绝原因',
  `review_time` datetime COMMENT '审核时间',
  `adopt_time` datetime COMMENT '领养时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_adopt_pet_id` (`pet_id`),
  CONSTRAINT `fk_adopt_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  KEY `idx_adopt_applicant_id` (`applicant_id`),
  CONSTRAINT `fk_adopt_applicant_id` FOREIGN KEY (`applicant_id`) REFERENCES `user` (`id`),
  KEY `idx_adopt_reviewer_id` (`reviewer_id`),
  CONSTRAINT `fk_adopt_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领养申请';

CREATE TABLE `breading` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '寄养申请',
  `pet_name` varchar(20) NOT NULL COMMENT '宠物名',
  `pet_age` int NOT NULL COMMENT '宠物年龄',
  `pet_type` varchar(20) NOT NULL COMMENT '宠物类型',
  `pet_breed` varchar(20) NOT NULL COMMENT '宠物品种',
  `pet_description` text NOT NULL COMMENT '宠物描述',
  `applicant_id` bigint NOT NULL COMMENT '申请人',
  `applicant_phone` varchar(20) NOT NULL COMMENT '申请人联系方式',
  `reviewer_id` bigint COMMENT '审核人',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `reject_reason` text COMMENT '审核拒绝原因',
  `review_time` datetime COMMENT '审核时间',
  `start_time` datetime COMMENT '生效时间',
  `end_time` datetime COMMENT '结束时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_breading_applicant_id` (`applicant_id`),
  CONSTRAINT `fk_breading_applicant_id` FOREIGN KEY (`applicant_id`) REFERENCES `user` (`id`),
  KEY `idx_breading_reviewer_id` (`reviewer_id`),
  CONSTRAINT `fk_breading_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='寄养申请';

CREATE TABLE `agreement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '领养/寄养协议',
  `parent_id` bigint NOT NULL COMMENT '申请 id',
  `parent_type` varchar(20) NOT NULL COMMENT '申请类型 (ADOPT / BREADING)',
  `content` text COMMENT '电子协议正文，null 表示纸质协议扫描',
  `type` varchar(20) NOT NULL COMMENT '协议类型',
  `sign` varchar(255) COMMENT '签名图片',
  `sign_time` datetime COMMENT '签署时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领养/寄养协议';

CREATE TABLE `agreement_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '协议文件（纸质扫描件 / 签名）',
  `agreement_id` bigint COMMENT '协议 id',
  `filename` varchar(255) NOT NULL COMMENT '文件名',
  `page` int NOT NULL COMMENT '页数，0 表示签名',
  `create_time` datetime NOT NULL COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_agreement_file_agreement_id` (`agreement_id`),
  CONSTRAINT `fk_agreement_file_agreement_id` FOREIGN KEY (`agreement_id`) REFERENCES `agreement` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议文件（纸质扫描件 / 签名）';

CREATE TABLE `agreement_update_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '协议更新记录',
  `agreement_id` bigint NOT NULL COMMENT '协议 id',
  `content` text COMMENT '原始协议内容',
  `type` varchar(20) NOT NULL COMMENT '更新类型',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_agreement_update_record_agreement_id` (`agreement_id`),
  CONSTRAINT `fk_agreement_update_record_agreement_id` FOREIGN KEY (`agreement_id`) REFERENCES `agreement` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议更新记录';

CREATE TABLE `follow_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '领养跟踪任务',
  `adopt_id` bigint NOT NULL COMMENT '申请 id',
  `worker_id` bigint NOT NULL COMMENT '安排人员',
  `volunteer_id` bigint COMMENT '志愿者 id',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `remark` text COMMENT '备注（如节假日顺延等）',
  `plan_time` datetime NOT NULL COMMENT '计划访问时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_follow_task_adopt_id` (`adopt_id`),
  CONSTRAINT `fk_follow_task_adopt_id` FOREIGN KEY (`adopt_id`) REFERENCES `adopt` (`id`),
  KEY `idx_follow_task_worker_id` (`worker_id`),
  CONSTRAINT `fk_follow_task_worker_id` FOREIGN KEY (`worker_id`) REFERENCES `user` (`id`),
  KEY `idx_follow_task_volunteer_id` (`volunteer_id`),
  CONSTRAINT `fk_follow_task_volunteer_id` FOREIGN KEY (`volunteer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领养跟踪任务';

CREATE TABLE `follow_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '领养跟踪记录',
  `task_id` bigint NOT NULL COMMENT '跟踪任务 id',
  `volunteer_id` bigint NOT NULL COMMENT '志愿者 id',
  `summary` varchar(255) NOT NULL COMMENT '简介状况',
  `visit_time` datetime NOT NULL COMMENT '回访时间',
  `life_status` text COMMENT '生活状态',
  `health_status` text COMMENT '健康状态',
  `risk` text COMMENT '虐待/弃养风险观察',
  `suggestion` text COMMENT '建议与反馈',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_follow_record_task_id` (`task_id`),
  CONSTRAINT `fk_follow_record_task_id` FOREIGN KEY (`task_id`) REFERENCES `follow_task` (`id`),
  KEY `idx_follow_record_volunteer_id` (`volunteer_id`),
  CONSTRAINT `fk_follow_record_volunteer_id` FOREIGN KEY (`volunteer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领养跟踪记录';

CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物资分类',
  `name` varchar(255) NOT NULL COMMENT '分类名称',
  `description` text COMMENT '分类说明',
  `is_discard` tinyint NOT NULL COMMENT '是否废弃',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物资分类';

CREATE TABLE `item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物品信息',
  `category_id` bigint NOT NULL COMMENT '分类 id',
  `name` varchar(255) NOT NULL COMMENT '物品名',
  `description` text NOT NULL COMMENT '物品描述',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `is_discard` tinyint NOT NULL COMMENT '已废弃',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_category_id` (`category_id`),
  CONSTRAINT `fk_item_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品信息';

CREATE TABLE `donation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '捐赠信息',
  `user_id` bigint NOT NULL COMMENT '捐赠人',
  `delivery` varchar(20) NOT NULL COMMENT '交付方式',
  `address` varchar(255) COMMENT '交付方式为 ADDRESS 时记录取货地址',
  `tracking_number` varchar(50) COMMENT '交付方式为 EXPRESS 时记录快递单号',
  `description` varchar(255) COMMENT '留言',
  `status` varchar(20) NOT NULL COMMENT '捐赠状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_donation_user_id` (`user_id`),
  CONSTRAINT `fk_donation_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='捐赠信息';

CREATE TABLE `donation_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '/',
  `donation_id` bigint NOT NULL COMMENT '捐赠 id',
  `type` varchar(20) NOT NULL COMMENT '文件类型',
  `filename` varchar(255) NOT NULL COMMENT '文件名',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_donation_file_donation_id` (`donation_id`),
  CONSTRAINT `fk_donation_file_donation_id` FOREIGN KEY (`donation_id`) REFERENCES `donation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='DonationFile';

CREATE TABLE `donation_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '捐赠物品表',
  `donation_id` bigint COMMENT '捐赠 id',
  `item_id` bigint COMMENT '物品 id',
  `item_name` varchar(255) COMMENT '若库中没有对应物品，手动填写物品名',
  `category_id` bigint NOT NULL COMMENT '物品类别 id',
  `count` decimal(10,2) NOT NULL COMMENT '物品数量',
  `unit` varchar(20) COMMENT '若库中没有对应物品，手动填写物品单位',
  `description` varchar(255) COMMENT '物品描述',
  `expire_time` datetime NOT NULL COMMENT '到期时间',
  `create_time` datetime COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_donation_item_donation_id` (`donation_id`),
  CONSTRAINT `fk_donation_item_donation_id` FOREIGN KEY (`donation_id`) REFERENCES `donation` (`id`),
  KEY `idx_donation_item_item_id` (`item_id`),
  CONSTRAINT `fk_donation_item_item_id` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`),
  KEY `idx_donation_item_category_id` (`category_id`),
  CONSTRAINT `fk_donation_item_category_id` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='捐赠物品表';

CREATE TABLE `donation_status_update_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '/',
  `donation_id` bigint NOT NULL COMMENT '捐赠 id',
  `user_id` bigint NOT NULL COMMENT '修改用户 id',
  `old_status` varchar(20) NOT NULL COMMENT '旧状态',
  `new_status` varchar(20) NOT NULL COMMENT '新状态',
  `reason` varchar(255) NOT NULL COMMENT '修改原因',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_donation_status_update_record_donation_id` (`donation_id`),
  CONSTRAINT `fk_donation_status_update_record_donation_id` FOREIGN KEY (`donation_id`) REFERENCES `donation` (`id`),
  KEY `idx_donation_status_update_record_user_id` (`user_id`),
  CONSTRAINT `fk_donation_status_update_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='DonationStatusUpdateRecord';

CREATE TABLE `stock` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存物品',
  `item_id` bigint NOT NULL COMMENT '对应物品',
  `user_id` bigint NOT NULL COMMENT '捐赠人/采购人 id',
  `count` decimal(10,2) NOT NULL COMMENT '数量',
  `source_type` varchar(20) NOT NULL COMMENT '来源类型',
  `expire_time` datetime NOT NULL COMMENT '有效期',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_stock_item_id` (`item_id`),
  CONSTRAINT `fk_stock_item_id` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`),
  KEY `idx_stock_user_id` (`user_id`),
  CONSTRAINT `fk_stock_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存物品';

CREATE TABLE `stock_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存流转记录',
  `stock_id` bigint NOT NULL COMMENT '关联库存',
  `user_id` bigint NOT NULL COMMENT '操作用户',
  `action` varchar(20) NOT NULL COMMENT '动作',
  `source_type` varchar(20) NOT NULL COMMENT '来源类型',
  `count` decimal(10,2) NOT NULL COMMENT '变动数量',
  `remain` decimal(10,2) NOT NULL COMMENT '剩余数量',
  `price` decimal(10,2) NOT NULL COMMENT '单价',
  `total_price` decimal(10,2) NOT NULL COMMENT '总价',
  `purpose` varchar(255) COMMENT '用途 / 销毁原因',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_stock_record_stock_id` (`stock_id`),
  CONSTRAINT `fk_stock_record_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`id`),
  KEY `idx_stock_record_user_id` (`user_id`),
  CONSTRAINT `fk_stock_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流转记录';

CREATE TABLE `subscribe` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存订阅',
  `element_id` bigint NOT NULL COMMENT '订阅 id（物品、类型、库存等）',
  `user_id` bigint NOT NULL COMMENT '订阅用户',
  `action` varchar(20) NOT NULL COMMENT '触发类型',
  `count` decimal(10,2) COMMENT '触发阈值',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_subscribe_user_id` (`user_id`),
  CONSTRAINT `fk_subscribe_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存订阅';

CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '处方',
  `allower_id` bigint NOT NULL COMMENT '申请人 id',
  `item_id` bigint NOT NULL COMMENT '物品 id',
  `parent_id` bigint NOT NULL COMMENT '与之关联的资源 id',
  `parent_type` varchar(20) NOT NULL COMMENT '关联类型',
  `type` varchar(20) NOT NULL COMMENT '处方类型',
  `count` decimal(10,5) NOT NULL COMMENT '数量',
  `unit` varchar(10) NOT NULL COMMENT '单位',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_allower_id` (`allower_id`),
  CONSTRAINT `fk_order_allower_id` FOREIGN KEY (`allower_id`) REFERENCES `user` (`id`),
  KEY `idx_order_item_id` (`item_id`),
  CONSTRAINT `fk_order_item_id` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='处方';

CREATE TABLE `lost_pet` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '走失宠物报备记录',
  `owner_id` bigint NOT NULL COMMENT '报备人 id',
  `name` varchar(255) NOT NULL COMMENT '宠物名称',
  `age` int COMMENT '宠物年龄（月）',
  `sex` varchar(10) NOT NULL COMMENT '宠物性别',
  `type` varchar(50) NOT NULL COMMENT '宠物类型',
  `breed` varchar(50) NOT NULL COMMENT '宠物品种',
  `features` text COMMENT '宠物特征描述（毛色、体型、特殊标记等）',
  `lost_time` datetime NOT NULL COMMENT '走失时间',
  `phone` varchar(50) NOT NULL COMMENT '联系方式',
  `description` text COMMENT '补充描述',
  `pet_id` bigint COMMENT '已找到的流浪宠物 id',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '最后一次修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_lost_pet_owner_id` (`owner_id`),
  CONSTRAINT `fk_lost_pet_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`),
  KEY `idx_lost_pet_pet_id` (`pet_id`),
  CONSTRAINT `fk_lost_pet_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='走失宠物报备记录';

CREATE TABLE `lost_pet_claim` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '走失宠物认领申请 / 记录',
  `lost_pet_id` bigint NOT NULL COMMENT '走失宠物报备 id',
  `pet_id` bigint COMMENT '找到的流浪宠物 id',
  `applicant_id` bigint NOT NULL COMMENT '认领申请人 id',
  `applicant_phone` varchar(50) NOT NULL COMMENT '申请人联系方式',
  `reviewer_id` bigint COMMENT '审核人（救助站工作人员）id',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `reason` text NOT NULL COMMENT '认领理由/证明材料描述',
  `approve_reason` text COMMENT '审核拒绝原因',
  `review_time` datetime COMMENT '审核时间',
  `claim_time` datetime COMMENT '认领完成时间（审核通过且归还完毕）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_lost_pet_claim_lost_pet_id` (`lost_pet_id`),
  CONSTRAINT `fk_lost_pet_claim_lost_pet_id` FOREIGN KEY (`lost_pet_id`) REFERENCES `lost_pet` (`id`),
  KEY `idx_lost_pet_claim_pet_id` (`pet_id`),
  CONSTRAINT `fk_lost_pet_claim_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  KEY `idx_lost_pet_claim_applicant_id` (`applicant_id`),
  CONSTRAINT `fk_lost_pet_claim_applicant_id` FOREIGN KEY (`applicant_id`) REFERENCES `user` (`id`),
  KEY `idx_lost_pet_claim_reviewer_id` (`reviewer_id`),
  CONSTRAINT `fk_lost_pet_claim_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='走失宠物认领申请 / 记录';

CREATE TABLE `lost_pet_mismatch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '走失宠物误匹配记录',
  `lost_pet_id` bigint NOT NULL COMMENT '走失宠物 id',
  `pet_id` bigint NOT NULL COMMENT '被排除的流浪宠物 id',
  `user_id` bigint NOT NULL COMMENT '操作用户 id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_lost_pet_mismatch_lost_pet_id` (`lost_pet_id`),
  CONSTRAINT `fk_lost_pet_mismatch_lost_pet_id` FOREIGN KEY (`lost_pet_id`) REFERENCES `lost_pet` (`id`),
  KEY `idx_lost_pet_mismatch_pet_id` (`pet_id`),
  CONSTRAINT `fk_lost_pet_mismatch_pet_id` FOREIGN KEY (`pet_id`) REFERENCES `pet` (`id`),
  KEY `idx_lost_pet_mismatch_user_id` (`user_id`),
  CONSTRAINT `fk_lost_pet_mismatch_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='走失宠物误匹配记录';

CREATE TABLE `volunteer_recruitment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '志愿者招募计划',
  `title` varchar(255) NOT NULL COMMENT '招募标题',
  `description` text COMMENT '招募说明',
  `requirement` text COMMENT '招募要求',
  `headcount` int NOT NULL COMMENT '招募人数',
  `applied_count` int NOT NULL COMMENT '已申请人数',
  `start_time` datetime NOT NULL COMMENT '招募开始时间',
  `end_time` datetime NOT NULL COMMENT '招募结束时间',
  `status` varchar(20) NOT NULL COMMENT '招募状态',
  `publisher_id` bigint NOT NULL COMMENT '发布人 id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer_recruitment_publisher_id` (`publisher_id`),
  CONSTRAINT `fk_volunteer_recruitment_publisher_id` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿者招募计划';

CREATE TABLE `volunteer_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '志愿者申请',
  `recruitment_id` bigint NOT NULL COMMENT '招募计划 id',
  `user_id` bigint NOT NULL COMMENT '申请人 id',
  `real_name` varchar(100) NOT NULL COMMENT '真实姓名',
  `sex` varchar(20) NOT NULL COMMENT '性别',
  `phone` varchar(50) NOT NULL COMMENT '联系电话',
  `age` int NOT NULL COMMENT '年龄',
  `experience` text NOT NULL COMMENT '过往经历',
  `skills` varchar(255) NOT NULL COMMENT '技能',
  `time_desc` varchar(255) NOT NULL COMMENT '可服务时间说明',
  `motivation` text NOT NULL COMMENT '申请动机',
  `status` varchar(20) NOT NULL COMMENT '申请状态',
  `reviewer_id` bigint COMMENT '审核人 id',
  `review_comment` text COMMENT '审核意见',
  `review_time` datetime COMMENT '审核时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer_application_recruitment_id` (`recruitment_id`),
  CONSTRAINT `fk_volunteer_application_recruitment_id` FOREIGN KEY (`recruitment_id`) REFERENCES `volunteer_recruitment` (`id`),
  KEY `idx_volunteer_application_user_id` (`user_id`),
  CONSTRAINT `fk_volunteer_application_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  KEY `idx_volunteer_application_reviewer_id` (`reviewer_id`),
  CONSTRAINT `fk_volunteer_application_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿者申请';

CREATE TABLE `volunteer_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '志愿者档案',
  `user_id` bigint NOT NULL COMMENT '用户 id',
  `status` varchar(20) NOT NULL COMMENT '档案状态',
  `real_name` varchar(100) NOT NULL COMMENT '真实姓名',
  `sex` varchar(20) NOT NULL COMMENT '性别',
  `phone` varchar(50) NOT NULL COMMENT '联系电话',
  `age` int NOT NULL COMMENT '年龄',
  `skills` text COMMENT '技能说明',
  `service_desc` text COMMENT '服务意向',
  `time_desc` varchar(255) COMMENT '可服务时间说明',
  `remark` text COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer_profile_user_id` (`user_id`),
  CONSTRAINT `fk_volunteer_profile_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿者档案';

CREATE TABLE `volunteer_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '志愿者任务',
  `task_type` varchar(20) NOT NULL COMMENT '任务类型',
  `task_id` bigint COMMENT '任务 id',
  `location_id` bigint COMMENT '任务地址 id',
  `title` varchar(255) NOT NULL COMMENT '任务标题',
  `content` text COMMENT '任务内容',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer_task_location_id` (`location_id`),
  CONSTRAINT `fk_volunteer_task_location_id` FOREIGN KEY (`location_id`) REFERENCES `location` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿者任务';

CREATE TABLE `volunteer_shift` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '志愿者排班',
  `volunteer_id` bigint NOT NULL COMMENT '志愿者 id',
  `assigner_id` bigint NOT NULL COMMENT '排班人 id',
  `task_id` bigint COMMENT '任务 id',
  `status` varchar(20) NOT NULL COMMENT '排班状态',
  `remark` text COMMENT '备注',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer_shift_volunteer_id` (`volunteer_id`),
  CONSTRAINT `fk_volunteer_shift_volunteer_id` FOREIGN KEY (`volunteer_id`) REFERENCES `user` (`id`),
  KEY `idx_volunteer_shift_assigner_id` (`assigner_id`),
  CONSTRAINT `fk_volunteer_shift_assigner_id` FOREIGN KEY (`assigner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿者排班';

CREATE TABLE `volunteer_shift_status_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '志愿者排班状态变更记录',
  `shift_id` bigint COMMENT '排班 id',
  `status_from` varchar(20) NOT NULL COMMENT '旧状态',
  `status_to` varchar(20) NOT NULL COMMENT '新状态Z',
  `comment` varchar(255) COMMENT '状态变更说明',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer_shift_status_record_shift_id` (`shift_id`),
  CONSTRAINT `fk_volunteer_shift_status_record_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `volunteer_shift` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿者排班状态变更记录';

CREATE TABLE `volunteer_service_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '志愿者服务记录',
  `shift_id` bigint NOT NULL COMMENT '排班 id',
  `volunteer_id` bigint NOT NULL COMMENT '志愿者 id',
  `start_time` datetime COMMENT '服务开始时间',
  `end_time` datetime COMMENT '服务结束时间',
  `actual_hours` decimal(10,2) COMMENT '实际服务时长',
  `summary` varchar(255) NOT NULL COMMENT '服务摘要',
  `content` text COMMENT '服务内容',
  `problem` text COMMENT '问题反馈',
  `suggestion` text COMMENT '改进建议',
  `status` varchar(20) NOT NULL COMMENT '记录状态',
  `reviewer_id` bigint COMMENT '审核人 id',
  `review_comment` text COMMENT '审核意见',
  `review_time` datetime COMMENT '审核时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer_service_record_shift_id` (`shift_id`),
  CONSTRAINT `fk_volunteer_service_record_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `volunteer_shift` (`id`),
  KEY `idx_volunteer_service_record_volunteer_id` (`volunteer_id`),
  CONSTRAINT `fk_volunteer_service_record_volunteer_id` FOREIGN KEY (`volunteer_id`) REFERENCES `user` (`id`),
  KEY `idx_volunteer_service_record_reviewer_id` (`reviewer_id`),
  CONSTRAINT `fk_volunteer_service_record_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿者服务记录';

CREATE TABLE `volunteer_reward` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '志愿者激励记录',
  `volunteer_id` bigint NOT NULL COMMENT '志愿者 id',
  `period_start` datetime NOT NULL COMMENT '统计开始时间',
  `period_end` datetime NOT NULL COMMENT '统计结束时间',
  `service_count` int COMMENT '服务次数',
  `total_hours` decimal(10,2) COMMENT '累计服务时长',
  `reward_type` varchar(20) NOT NULL COMMENT '激励类型',
  `reward_value` varchar(255) COMMENT '激励内容',
  `reward_reason` text COMMENT '激励原因',
  `status` varchar(20) NOT NULL COMMENT '激励状态',
  `issuer_id` bigint COMMENT '发放人 id',
  `issue_time` datetime COMMENT '发放时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer_reward_volunteer_id` (`volunteer_id`),
  CONSTRAINT `fk_volunteer_reward_volunteer_id` FOREIGN KEY (`volunteer_id`) REFERENCES `user` (`id`),
  KEY `idx_volunteer_reward_issuer_id` (`issuer_id`),
  CONSTRAINT `fk_volunteer_reward_issuer_id` FOREIGN KEY (`issuer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿者激励记录';

CREATE TABLE `article` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '宣传文章',
  `type` varchar(20) NOT NULL COMMENT '文章类型',
  `status` varchar(20) NOT NULL COMMENT '文章状态',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '正文',
  `cover` varchar(255) COMMENT '封面',
  `author_id` bigint NOT NULL COMMENT '作者 id',
  `is_discard` tinyint NOT NULL COMMENT '已弃用',
  `view_count` int NOT NULL COMMENT '浏览量',
  `like_count` int NOT NULL COMMENT '点赞数',
  `share_count` int NOT NULL COMMENT '分享数',
  `publish_time` datetime COMMENT '发布时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_author_id` (`author_id`),
  CONSTRAINT `fk_article_author_id` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宣传文章';

CREATE TABLE `article_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章点赞记录',
  `article_id` bigint NOT NULL COMMENT '文章 id',
  `user_id` bigint NOT NULL COMMENT '用户 id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_like_article_id` (`article_id`),
  CONSTRAINT `fk_article_like_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  KEY `idx_article_like_user_id` (`user_id`),
  CONSTRAINT `fk_article_like_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞记录';

CREATE TABLE `article_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章收藏记录',
  `article_id` bigint NOT NULL COMMENT '文章 id',
  `user_id` bigint NOT NULL COMMENT '用户 id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_favorite_article_id` (`article_id`),
  CONSTRAINT `fk_article_favorite_article_id` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  KEY `idx_article_favorite_user_id` (`user_id`),
  CONSTRAINT `fk_article_favorite_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章收藏记录';

CREATE TABLE `delete_job` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '/',
  `path` varchar(255) NOT NULL COMMENT '文件路径',
  `status` varchar(20) NOT NULL COMMENT '任务状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `start_time` datetime COMMENT '开始时间',
  `finish_time` datetime COMMENT '结束时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='DeleteJob';

SET FOREIGN_KEY_CHECKS = 1;

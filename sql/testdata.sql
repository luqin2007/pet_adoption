USE `pet_adoption`;

SET FOREIGN_KEY_CHECKS = 0;

-- 流浪宠物演示数据，用于前端页面联调与首页待领养推荐展示。
INSERT INTO `user` (`id`, `username`, `password`, `email`, `role`, `avatar`, `phone`, `create_time`, `update_time`) VALUES
    (3000000000000000001, 'seed_rescuer', 'seed-data-not-for-login', 'seed-rescuer@example.test', 2, NULL, '0571-00000000', NOW(), NOW())
ON DUPLICATE KEY UPDATE
                     `username` = VALUES(`username`),
                     `email` = VALUES(`email`),
                     `role` = VALUES(`role`),
                     `phone` = VALUES(`phone`),
                     `update_time` = NOW();

INSERT INTO `pet` (`id`, `discover_id`, `name`, `age`, `sex`, `type`, `breed`, `health`, `description`, `status`, `is_discard`, `create_time`, `update_time`) VALUES
                                                                                                                                                                  (3000000000000001001, 3000000000000000001, '橘子', 8, '公', '猫', '中华田园猫', '已完成基础体检，轻微营养不良', '在社区花坛附近被发现，亲人但有些怕生，适合安静家庭。', 'HEALTH', 0, NOW(), NOW()),
                                                                                                                                                                  (3000000000000001002, 3000000000000000001, '糯米', 14, '母', '狗', '田园犬', '已驱虫，皮肤恢复中', '夜间巡护时发现，会主动跟随志愿者，性格稳定。', 'SHELTERED', 0, NOW(), NOW()),
                                                                                                                                                                  (3000000000000001003, 3000000000000000001, '小煤球', 4, '未知', '猫', '黑猫', '待体检', '发现于停车棚附近，年龄较小，正在观察进食情况。', 'WAITING', 0, NOW(), NOW()),
                                                                                                                                                                  (3000000000000001004, 3000000000000000001, '豆沙', 20, '公', '狗', '柯基混血', '已绝育，精神状态良好', '疑似走失后长期流浪，已完成初步安置。', 'FINDING', 0, NOW(), NOW()),
                                                                                                                                                                  (3000000000000001005, 3000000000000000001, '雪糕', 10, '母', '猫', '长毛猫', '眼部轻微发炎，治疗中', '居民楼下连续出现三天，毛发打结，需要持续护理。', 'SHELTERED', 0, NOW(), NOW()),
                                                                                                                                                                  (3000000000000001006, 3000000000000000001, '栗子', 6, '公', '狗', '拉布拉多混血', '疫苗待补', '公园入口附近发现，体型中等，亲人活泼。', 'HEALTH', 0, NOW(), NOW()),
                                                                                                                                                                  (3000000000000001007, 3000000000000000001, '月饼', 3, '未知', '猫', '奶牛猫', '幼猫观察期', '与同窝幼猫走散，暂由救助站隔离照护。', 'WAITING', 0, NOW(), NOW()),
                                                                                                                                                                  (3000000000000001008, 3000000000000000001, '阿福', 30, '公', '狗', '金毛混血', '体检正常，已驱虫', '性格温和，会基础随行，适合有养犬经验家庭。', 'HEALTH', 0, NOW(), NOW()),
                                                                                                                                                                  (3000000000000001009, 3000000000000000001, '珍珠', 18, '母', '猫', '三花猫', '已绝育，恢复良好', '学校后门附近定点喂养后收容，适应能力较好。', 'ADOPTED', 0, NOW(), NOW()),
                                                                                                                                                                  (3000000000000001010, 3000000000000000001, '布丁', 12, '公', '狗', '比熊混血', '皮毛护理中，食欲正常', '雨天在商铺门口避雨，被爱心人士临时安置。', 'SHELTERED', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
                     `discover_id` = VALUES(`discover_id`),
                     `name` = VALUES(`name`),
                     `age` = VALUES(`age`),
                     `sex` = VALUES(`sex`),
                     `type` = VALUES(`type`),
                     `breed` = VALUES(`breed`),
                     `health` = VALUES(`health`),
                     `description` = VALUES(`description`),
                     `status` = VALUES(`status`),
                     `is_discard` = VALUES(`is_discard`),
                     `update_time` = NOW();

INSERT INTO `location` (`id`, `parent_id`, `parent_type`, `user_id`, `province`, `city`, `district`, `detail_address`, `create_time`) VALUES
                                                                                                                                          (3000000000000002001, 3000000000000001001, 'PET', 3000000000000000001, '浙江省', '杭州市', '西湖区', '文三路社区花坛旁', NOW()),
                                                                                                                                          (3000000000000002002, 3000000000000001002, 'PET', 3000000000000000001, '浙江省', '杭州市', '拱墅区', '运河广场东侧步道', NOW()),
                                                                                                                                          (3000000000000002003, 3000000000000001003, 'PET', 3000000000000000001, '浙江省', '杭州市', '滨江区', '星耀城停车棚', NOW()),
                                                                                                                                          (3000000000000002004, 3000000000000001004, 'PET', 3000000000000000001, '浙江省', '杭州市', '上城区', '清泰街地铁口附近', NOW()),
                                                                                                                                          (3000000000000002005, 3000000000000001005, 'PET', 3000000000000000001, '浙江省', '杭州市', '余杭区', '未来科技城居民楼下', NOW()),
                                                                                                                                          (3000000000000002006, 3000000000000001006, 'PET', 3000000000000000001, '浙江省', '杭州市', '萧山区', '湘湖公园入口', NOW()),
                                                                                                                                          (3000000000000002007, 3000000000000001007, 'PET', 3000000000000000001, '浙江省', '杭州市', '临平区', '东湖街道快递站旁', NOW()),
                                                                                                                                          (3000000000000002008, 3000000000000001008, 'PET', 3000000000000000001, '浙江省', '杭州市', '钱塘区', '金沙湖绿道', NOW()),
                                                                                                                                          (3000000000000002009, 3000000000000001009, 'PET', 3000000000000000001, '浙江省', '杭州市', '西湖区', '高校后门便利店旁', NOW()),
                                                                                                                                          (3000000000000002010, 3000000000000001010, 'PET', 3000000000000000001, '浙江省', '杭州市', '富阳区', '桂花西路商铺门口', NOW())
ON DUPLICATE KEY UPDATE
                     `parent_id` = VALUES(`parent_id`),
                     `parent_type` = VALUES(`parent_type`),
                     `user_id` = VALUES(`user_id`),
                     `province` = VALUES(`province`),
                     `city` = VALUES(`city`),
                     `district` = VALUES(`district`),
                     `detail_address` = VALUES(`detail_address`);

INSERT INTO `media_file` (`id`, `parent_id`, `parent_type`, `user_id`, `name`, `description`, `is_cover`, `filename`, `type`, `create_time`) VALUES
                                                                                                                                                 (3000000000000003001, 3000000000000001001, 'PET', 3000000000000000001, '橘子封面', 'Pexels 流浪猫测试图', 1, 'seed_pet_01.jpg', 'IMAGE', NOW()),
                                                                                                                                                 (3000000000000003002, 3000000000000001002, 'PET', 3000000000000000001, '糯米封面', 'Pexels 流浪犬测试图', 1, 'seed_pet_02.jpg', 'IMAGE', NOW()),
                                                                                                                                                 (3000000000000003003, 3000000000000001003, 'PET', 3000000000000000001, '小煤球封面', 'Picsum 临时占位图', 1, 'seed_pet_03.jpg', 'IMAGE', NOW()),
                                                                                                                                                 (3000000000000003004, 3000000000000001004, 'PET', 3000000000000000001, '豆沙封面', 'Pexels 犬类测试图', 1, 'seed_pet_04.jpg', 'IMAGE', NOW()),
                                                                                                                                                 (3000000000000003005, 3000000000000001005, 'PET', 3000000000000000001, '雪糕封面', 'Pexels 猫咪测试图', 1, 'seed_pet_05.jpg', 'IMAGE', NOW()),
                                                                                                                                                 (3000000000000003006, 3000000000000001006, 'PET', 3000000000000000001, '栗子封面', 'Pexels 犬类测试图', 1, 'seed_pet_06.jpg', 'IMAGE', NOW()),
                                                                                                                                                 (3000000000000003007, 3000000000000001007, 'PET', 3000000000000000001, '月饼封面', 'Picsum 临时占位图', 1, 'seed_pet_07.jpg', 'IMAGE', NOW()),
                                                                                                                                                 (3000000000000003008, 3000000000000001008, 'PET', 3000000000000000001, '阿福封面', 'Pexels 金毛测试图', 1, 'seed_pet_08.jpg', 'IMAGE', NOW()),
                                                                                                                                                 (3000000000000003009, 3000000000000001009, 'PET', 3000000000000000001, '珍珠封面', 'Pexels 猫咪测试图', 1, 'seed_pet_09.jpg', 'IMAGE', NOW()),
                                                                                                                                                 (3000000000000003010, 3000000000000001010, 'PET', 3000000000000000001, '布丁封面', 'Picsum 临时占位图', 1, 'seed_pet_10.jpg', 'IMAGE', NOW())
ON DUPLICATE KEY UPDATE
                     `parent_id` = VALUES(`parent_id`),
                     `parent_type` = VALUES(`parent_type`),
                     `user_id` = VALUES(`user_id`),
                     `name` = VALUES(`name`),
                     `description` = VALUES(`description`),
                     `is_cover` = VALUES(`is_cover`),
                     `filename` = VALUES(`filename`),
                     `type` = VALUES(`type`);

INSERT IGNORE INTO `info_pet_type` (`type`, `breed`, `create_time`)
SELECT DISTINCT TRIM(`type`), COALESCE(TRIM(`breed`), ''), NOW()
FROM `pet`
WHERE `type` IS NOT NULL AND TRIM(`type`) <> '';

INSERT IGNORE INTO `info_pet_type` (`type`, `breed`, `create_time`)
SELECT DISTINCT TRIM(`type`), COALESCE(TRIM(`breed`), ''), NOW()
FROM `lost_pet`
WHERE `type` IS NOT NULL AND TRIM(`type`) <> '';

INSERT IGNORE INTO `info_pet_type` (`type`, `breed`, `create_time`)
SELECT DISTINCT TRIM(`pet_type`), COALESCE(TRIM(`pet_breed`), ''), NOW()
FROM `breading`
WHERE `type` IS NOT NULL AND TRIM(`type`) <> '';

SET FOREIGN_KEY_CHECKS = 1;

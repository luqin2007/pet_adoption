ALTER TABLE `adopt`
    MODIFY COLUMN `status` varchar(32) NOT NULL COMMENT '状态';

ALTER TABLE `breading`
    MODIFY COLUMN `status` varchar(32) NOT NULL COMMENT '状态';

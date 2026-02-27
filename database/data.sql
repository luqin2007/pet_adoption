USE `pet_adoption`;

SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO `user` (`username`, `password`, `email`, `role`, `avatar`, `createTime`, `updateTime`) VALUES
('admin', '$2a$10$tidhnWgrXIMMlhtPN1tnj.DYTGkWJAU/UkU85KnGPErkmFq/7u5fK', 'lqjhzp@163.com', 16, NULL, NOW(), NOW())

SET FOREIGN_KEY_CHECKS = 1;
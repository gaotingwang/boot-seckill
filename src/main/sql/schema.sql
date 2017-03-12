-- 创建数据库
CREATE DATABASE seckill;
-- 使用数据库
USE seckill;

-- 创建秒杀库存表
CREATE TABLE `seckill`(
  `seckill_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品库存ID',
  `name` VARCHAR(120) NOT NULL COMMENT '商品库存名称',
  `number` INT NOT NULL COMMENT '商品库存数',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `start_time` TIMESTAMP NOT NULL COMMENT '秒杀开始时间',
  `end_time` TIMESTAMP NOT NULL COMMENT '秒杀结束时间',
  PRIMARY KEY (`seckill_id`),
  KEY idx_create_time(`create_time`),
  KEY idx_start_time(`start_time`),
  KEY idx_end_time(`end_time`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='秒杀库存表';
-- 初始化库存表数据
INSERT INTO `seckill` (`name`, `number`, `start_time`, `end_time`)
VALUES ('5000元秒杀MacBook', 100, '2017-03-05 00:00:00', '2017-03-06 00:00:00'),
('1000元秒杀iphone', 200, '2017-03-05 00:00:00', '2017-03-06 00:00:00'),
('500元秒杀华为', 300, '2017-03-05 00:00:00', '2017-03-06 00:00:00'),
('100元秒杀小米', 400, '2017-03-05 00:00:00', '2017-03-06 00:00:00');

-- 创建秒杀成功明细表
CREATE TABLE `success_killed`(
  `seckill_id` BIGINT NOT NULL COMMENT '秒杀成功商品ID',
  `user_phone` BIGINT NOT NULL COMMENT '秒杀成功的用户手机号',
  `state` TINYINT NOT NULL DEFAULT -1 COMMENT '秒杀标识:-1 无效,0 成功,1 已付款',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`seckill_id`, `user_phone`),
  KEY idx_create_time(`create_time`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='秒杀成功明细表';
-- ============================================================
-- emp-mis 数据库初始化脚本（MySQL 8.x）
-- 步骤 4.1：数据库精细化设计
--   1) emp_name 建立普通索引 idx_emp_name（按姓名查询走索引）
--   2) 字段约束：gender/department/salary/hire_date 非空 + 默认值，
--      email 唯一索引 uk_email（允许 NULL，NULL 不参与唯一约束）
--   3) 表级：InnoDB + utf8mb4，主键自增
-- 注意：本脚本为初始化脚本，执行会重建 emp 表并重置示例数据
-- ============================================================

CREATE DATABASE IF NOT EXISTS emp_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE emp_db;

-- 重建员工表（幂等：先删后建）
DROP TABLE IF EXISTS emp;

CREATE TABLE emp (
    emp_id      INT            NOT NULL AUTO_INCREMENT COMMENT '员工编号（自增主键）',
    emp_name    VARCHAR(50)    NOT NULL COMMENT '员工姓名',
    gender      CHAR(2)        NOT NULL DEFAULT '男' COMMENT '性别（男/女）',
    department  VARCHAR(50)    NOT NULL DEFAULT '未分配' COMMENT '部门',
    job_title   VARCHAR(50)    DEFAULT NULL COMMENT '岗位',
    email       VARCHAR(100)   DEFAULT NULL COMMENT '邮箱（唯一）',
    phone       VARCHAR(20)    DEFAULT NULL COMMENT '电话',
    salary      DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '薪资',
    hire_date   DATE           NOT NULL DEFAULT (CURRENT_DATE) COMMENT '入职日期（默认当天）',
    PRIMARY KEY (emp_id),
    KEY idx_emp_name (emp_name),
    UNIQUE KEY uk_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '员工信息表';

-- 示例数据（8 条，覆盖 4 个部门，便于测试动态条件/分页）
INSERT INTO emp (emp_name, gender, department, job_title, email, phone, salary, hire_date) VALUES
('张伟', '男', '技术部', '高级工程师', 'zhangwei@lab.com',  '13800000001', 18000.00, '2019-03-15'),
('李娜', '女', '技术部', '前端工程师', 'lina@lab.com',      '13800000002', 15000.00, '2020-07-01'),
('王强', '男', '技术部', '测试工程师', 'wangqiang@lab.com', '13800000003', 12000.00, '2021-05-20'),
('刘洋', '男', '市场部', '市场经理',   'liuyang@lab.com',   '13800000004', 16000.00, '2018-11-08'),
('陈静', '女', '市场部', '市场专员',   'chenjing@lab.com',  '13800000005',  9000.00, '2022-02-14'),
('杨丽', '女', '人事部', 'HR主管',     'yangli@lab.com',    '13800000006', 14000.00, '2017-09-01'),
('赵敏', '女', '财务部', '会计',       'zhaomin@lab.com',   '13800000007', 13000.00, '2020-01-10'),
('孙浩', '男', '财务部', '出纳',       'sunhao@lab.com',    '13800000008',  8500.00, '2023-06-30');

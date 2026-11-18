-- ============================================================
-- EMP 员工管理 MIS 系统建库脚本（MySQL 8.x）
-- 数据库沿用实验一：ssm_emp 库 emp 表
-- 说明：
--   1) 本脚本与服务器上现有 ssm_emp.emp 结构一致，用于版本管理与环境重建；
--   2) 采用 CREATE ... IF NOT EXISTS，不会破坏已有数据；
--   3) emp_name 普通索引：沿用实验一的旧表可能没有该索引，而 MySQL 8 没有
--      CREATE INDEX IF NOT EXISTS，故脚本末尾用 information_schema 判断后动态补建，
--      保证新库与旧库都能达到"姓名模糊查询走索引"的性能要求；
--   4) status 列默认值 1（在职），演示"默认值"约束；
--   5) 拓展：dept 部门表与 emp 一对多关联。关联键为部门名称（emp.dept 是
--      名称字符串且实验一表结构不可变更），dept_name 唯一索引保证一对多
--      语义；级联保护在应用层实现（删部门前检查在职员工、删员工不影响部门）。
-- ============================================================

CREATE DATABASE IF NOT EXISTS ssm_emp
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE ssm_emp;

CREATE TABLE IF NOT EXISTS emp (
    emp_id      INT            NOT NULL AUTO_INCREMENT COMMENT '员工编号（自增主键）',
    emp_name    VARCHAR(50)    NOT NULL COMMENT '员工姓名',
    gender      CHAR(2)        DEFAULT NULL COMMENT '性别（男/女）',
    dept        VARCHAR(50)    DEFAULT NULL COMMENT '部门',
    post        VARCHAR(50)    DEFAULT NULL COMMENT '岗位',
    salary      DECIMAL(10, 2) DEFAULT NULL COMMENT '薪资',
    hire_date   DATE           DEFAULT NULL COMMENT '入职日期',
    status      INT            DEFAULT 1 COMMENT '在职状态：1 在职，0 离职',
    PRIMARY KEY (emp_id),
    KEY idx_emp_name (emp_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '员工信息表';

-- 示例数据（表内已有数据时由执行器 Step41SqlRunner 自动跳过，防止重复插入）
INSERT INTO emp (emp_name, gender, dept, post, salary, hire_date, status) VALUES
('张三', '男', '技术部', '后端工程师', 12000.00, '2022-03-01', 1),
('李四', '女', '市场部', '销售专员',    8000.00, '2021-07-15', 1),
('王五', '男', '技术部', '前端工程师', 11000.00, '2023-01-10', 1),
('赵六', '女', '人事部', 'HR专员',      7000.00, '2023-08-21', 1);

-- ------------------------------------------------------------
-- 拓展：dept 部门表（与 emp 一对多：一个部门有多名员工）
-- 关联方式：dept.dept_name = emp.dept（按名称关联，emp 表零改动）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS dept (
    dept_no     INT         NOT NULL AUTO_INCREMENT COMMENT '部门编号（自增主键）',
    dept_name   VARCHAR(50) NOT NULL COMMENT '部门名称（唯一，关联 emp.dept）',
    dept_desc   VARCHAR(100) DEFAULT NULL COMMENT '部门描述',
    create_date DATE        NOT NULL DEFAULT (CURRENT_DATE) COMMENT '创建日期',
    PRIMARY KEY (dept_no),
    UNIQUE KEY uk_dept_name (dept_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '部门表（一对多拓展）';

-- 从现有员工表同步部门（幂等：已存在的部门名跳过，不破坏手工维护的数据）
INSERT IGNORE INTO dept (dept_name, dept_desc)
SELECT DISTINCT dept, CONCAT(dept, '（由 emp 表自动同步）')
FROM emp
WHERE dept IS NOT NULL AND dept <> ''
  AND dept NOT IN (SELECT dept_name FROM dept);

-- ------------------------------------------------------------
-- 性能优化（步骤 4.1）：emp_name 普通索引，支撑姓名模糊查询
-- 沿用实验一的旧表可能不存在该索引，MySQL 8 又不支持
-- CREATE INDEX IF NOT EXISTS，故先查 information_schema 再动态执行 DDL，
-- 使脚本可重复执行（索引已存在时只打印提示，不报错、不重复建）
-- ------------------------------------------------------------
SET @idx_exists = (SELECT COUNT(DISTINCT INDEX_NAME) FROM information_schema.STATISTICS
                   WHERE TABLE_SCHEMA = 'ssm_emp'
                     AND TABLE_NAME = 'emp'
                     AND INDEX_NAME = 'idx_emp_name');

SET @idx_ddl = IF(@idx_exists = 0,
                  'ALTER TABLE emp ADD INDEX idx_emp_name (emp_name)',
                  'SELECT ''idx_emp_name 已存在，跳过建索引'' AS msg');

PREPARE idx_stmt FROM @idx_ddl;
EXECUTE idx_stmt;
DEALLOCATE PREPARE idx_stmt;

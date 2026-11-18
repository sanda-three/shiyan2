package com.lab.emp.service;

import com.lab.emp.entity.Emp;

/**
 * 任务 6.5 声明式事务验证服务（教学演示用，不参与正式业务接口）
 * <p>
 * 五组方法构成一张对照表：只有第 1 组是"事务正常生效"，其余都是
 * "事务按预期回滚失败"的参照组与失效场景，方便逐一记录现象与原因。
 * 所有写入都由调用方（测试工具/接口）随后用 countByName 观察数据库实际状态。
 */
public interface TxVerifyService {

    /**
     * 场景 1（正例）：加 @Transactional(rollbackFor = Exception.class)，
     * 插入后制造 1/0 运行时异常 —— 预期数据回滚，数据库查不到该员工
     */
    void saveWithTx(Emp emp);

    /**
     * 场景 2（对照组）：方法体与场景 1 完全相同，唯一区别是不加 @Transactional。
     * 预期插入语句被 JDBC 自动提交，异常照样抛出，但数据留在库里 —— 证明
     * "回滚"这件事完全由 @Transactional + 事务管理器决定
     */
    void saveWithoutTx(Emp emp);

    /**
     * 场景 3（失效：异常被吞）：有 @Transactional，但 try-catch 把异常吃掉，
     * 方法正常返回。Spring 只能在事务切面外层感知到"方法抛异常"才标记回滚，
     * 异常被吞后切面认为执行成功 —— 预期数据不回滚
     *
     * @return true 表示异常确实被 catch 吞掉（方法没有抛出）
     */
    boolean saveSwallowError(Emp emp);

    /**
     * 场景 4（失效：this 自调用绕过代理）：本方法无事务注解，方法体内用
     * this.saveWithTx(...) 调用同类中有事务注解的方法。@Transactional 靠 AOP
     * 代理生效，自调用不经过代理对象，事务通知根本没被触发 —— 预期不回滚
     */
    void saveViaSelfInvocation(Emp emp);

    /**
     * 场景 5（失效：注解加在 private 方法上）：public 方法调用同类 private 的
     * @Transactional 方法。Spring 默认用 CGLIB/动态代理，只能代理 public 方法，
     * private 方法上的注解被静默忽略 —— 预期不回滚
     */
    void saveViaPrivateAnnotated(Emp emp);

    /**
     * 按姓名统计数据库实际行数，用于观察每种场景到底提交了几条数据
     */
    long countByName(String empName);

    /**
     * 清理验证数据：按姓名删除，返回删除条数（仅供测试工具复位数据使用）
     */
    int deleteByName(String empName);
}

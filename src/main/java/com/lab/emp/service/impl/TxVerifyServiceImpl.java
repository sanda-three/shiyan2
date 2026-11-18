package com.lab.emp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.EmpMapper;
import com.lab.emp.service.TxVerifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 任务 6.5 声明式事务验证实现
 * <p>
 * 现象记录（实测结论，配合 StepTxVerifyTool 观察）：
 * <pre>
 * 场景                     事务是否生效        数据是否留下     原因
 * saveWithTx               生效（回滚）        否              代理开启事务，异常传出 -> rollback
 * saveWithoutTx            未开启              是              无注解，连接自动提交，insert 立即落库
 * saveSwallowError         生效（但不回滚）    是              异常被 catch，切面认为方法正常结束 -> commit
 * saveViaSelfInvocation    未生效              是              this 调用绕过代理，事务通知未被触发
 * saveViaPrivateAnnotated  未生效              是              private 方法无法被 CGLIB 代理，注解被忽略
 * </pre>
 * 这里刻意直接注入 EmpMapper 而不是继承 ServiceImpl，是为了让每种场景的事务
 * 边界只由本类方法上的注解决定，不受父类方法自带事务的干扰。
 */
@Service
public class TxVerifyServiceImpl implements TxVerifyService {

    private static final Logger log = LoggerFactory.getLogger(TxVerifyServiceImpl.class);

    @Autowired
    private EmpMapper empMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithTx(Emp emp) {
        empMapper.insert(emp);
        log.info("[TX-1] insert 成功，id={}，接着制造 ArithmeticException", emp.getEmpId());
        int i = 1 / 0;   // 制造运行时异常，验证回滚
    }

    @Override
    public void saveWithoutTx(Emp emp) {
        // 与场景 1 逐行相同，仅缺少 @Transactional
        empMapper.insert(emp);
        log.info("[TX-2] 无事务注解，insert 已自动提交，id={}", emp.getEmpId());
        int i = 1 / 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSwallowError(Emp emp) {
        empMapper.insert(emp);
        log.info("[TX-3] insert 成功，id={}，异常即将被 try-catch 吞掉", emp.getEmpId());
        try {
            int i = 1 / 0;
        } catch (ArithmeticException e) {
            // 只记录日志、不再抛出：事务切面感知不到异常，于是照常 commit
            log.warn("[TX-3] 异常被吞掉：{}", e.getMessage());
            return true;
        }
        return false;
    }

    @Override
    public void saveViaSelfInvocation(Emp emp) {
        // this. 调用的是目标对象本身而非代理对象，@Transactional 的环绕通知不会执行。
        // 编译期不会报错，IDE 也不会告警，是实际项目中最常见的事务失效写法。
        this.saveWithTx(emp);
    }

    @Override
    public void saveViaPrivateAnnotated(Emp emp) {
        doInsertThenFailPrivate(emp);
    }

    /**
     * private + @Transactional：Spring 基于代理的 AOP 只能拦截 public 方法，
     * 此处的注解被静默忽略（启动时不报错，日志也无提示），事务根本没开启
     */
    @Transactional(rollbackFor = Exception.class)
    private void doInsertThenFailPrivate(Emp emp) {
        empMapper.insert(emp);
        log.info("[TX-5] private 方法内 insert 成功，id={}", emp.getEmpId());
        int i = 1 / 0;
    }

    @Override
    public long countByName(String empName) {
        LambdaQueryWrapper<Emp> wrapper = new LambdaQueryWrapper<Emp>()
                .eq(Emp::getEmpName, empName);
        return empMapper.selectCount(wrapper);
    }

    @Override
    public int deleteByName(String empName) {
        LambdaQueryWrapper<Emp> wrapper = new LambdaQueryWrapper<Emp>()
                .eq(Emp::getEmpName, empName);
        return empMapper.delete(wrapper);
    }
}

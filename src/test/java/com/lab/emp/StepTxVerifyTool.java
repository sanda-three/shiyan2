package com.lab.emp;

import com.lab.emp.config.SpringConfig;
import com.lab.emp.entity.Emp;
import com.lab.emp.service.EmpService;
import com.lab.emp.service.TxVerifyService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;

/**
 * 任务 6.5 声明式事务验证工具
 * <p>
 * 每种场景使用独立姓名，执行后立即查询该姓名的实际落库行数：
 * count=0 说明数据被回滚；count=1 说明数据留在了库里（事务未开启，或开了但没回滚）。
 * 全部验证结束后按姓名清理，不改动 ssm_emp 原有 4 条数据。
 */
public class StepTxVerifyTool {

    /** 五种场景各用一个独立姓名，便于按姓名统计落库结果 */
    private static final String[] TEST_NAMES = {
            "事务测试1回滚", "事务测试2未加注解", "事务测试3吞异常",
            "事务测试4自调用", "事务测试5私有注解"};

    public static void main(String[] args) {
        AnnotationConfigApplicationContext root = new AnnotationConfigApplicationContext();
        root.register(SpringConfig.class);
        root.refresh();

        // 必须使用容器返回的 Bean（AOP 代理对象），不能 new 目标类，
        // 否则连场景 1 都没有事务，验证结论就失真了
        TxVerifyService tx = root.getBean(TxVerifyService.class);
        EmpService empService = root.getBean(EmpService.class);

        System.out.println("========== 任务 6.5 声明式事务验证（库：ssm_emp，表：emp）==========");
        System.out.println("[PROXY] 容器返回的 Bean 代理类：" + tx.getClass().getName());
        System.out.println();

        long before = empService.count();
        System.out.println("[PRE] emp 表总行数 = " + before);
        System.out.println();

        // ---- 场景 1：@Transactional + 运行时异常 -> 预期回滚 ----
        check(tx, "场景1 @Transactional(rollbackFor) + 1/0 异常（正例）", "事务测试1回滚", () -> {
            try {
                tx.saveWithTx(newEmp("事务测试1回滚"));
            } catch (Exception e) {
                System.out.println("       异常已传出事务切面：" + e.getClass().getSimpleName()
                        + " -> " + e.getMessage());
            }
        });

        // ---- 场景 2：代码相同但不加注解（对照组）----
        check(tx, "场景2 同样的代码但不加 @Transactional（对照组）", "事务测试2未加注解", () -> {
            try {
                tx.saveWithoutTx(newEmp("事务测试2未加注解"));
            } catch (Exception e) {
                System.out.println("       异常照样抛出：" + e.getClass().getSimpleName()
                        + " -> " + e.getMessage());
            }
        });

        // ---- 场景 3：异常被 try-catch 吞掉 ----
        check(tx, "场景3 有注解但异常被 try-catch 吞掉（失效场景一）", "事务测试3吞异常", () -> {
            boolean swallowed = tx.saveSwallowError(newEmp("事务测试3吞异常"));
            System.out.println("       方法正常返回，异常是否被吞掉 = " + swallowed
                    + "（切面感知不到异常，事务照常 commit）");
        });

        // ---- 场景 4：this 自调用绕过代理 ----
        check(tx, "场景4 this 自调用 @Transactional 方法（失效场景二）", "事务测试4自调用", () -> {
            try {
                tx.saveViaSelfInvocation(newEmp("事务测试4自调用"));
            } catch (Exception e) {
                System.out.println("       异常来自内层 saveWithTx：" + e.getClass().getSimpleName()
                        + "，但这次调用没经过代理 -> " + e.getMessage());
            }
        });

        // ---- 场景 5：注解加在 private 方法上 ----
        check(tx, "场景5 @Transactional 加在 private 方法（失效场景三）", "事务测试5私有注解", () -> {
            try {
                tx.saveViaPrivateAnnotated(newEmp("事务测试5私有注解"));
            } catch (Exception e) {
                System.out.println("       异常照样抛出：" + e.getClass().getSimpleName()
                        + "，但 private 方法无法被代理 -> " + e.getMessage());
            }
        });

        System.out.println();
        long after = empService.count();
        System.out.println("[SUM] emp 表总行数 " + before + " -> " + after + "，净增 "
                + (after - before) + " 条（5 次插入只有场景 1 回滚，预期净增 4）");

        // ---- 清理验证数据，复位 ----
        int cleaned = 0;
        for (String n : TEST_NAMES) {
            cleaned += tx.deleteByName(n);
        }
        System.out.println("[CLEAN] 删除验证数据 " + cleaned + " 条，复位后 emp 表总行数 = "
                + empService.count() + "（应为 4）");

        root.close();
    }

    /** 构造一条验证用员工数据（各场景仅姓名不同） */
    private static Emp newEmp(String name) {
        Emp e = new Emp();
        e.setEmpName(name);
        e.setGender("男");
        e.setDept("技术部");
        e.setPost("事务验证岗");
        e.setSalary(new BigDecimal("9999.00"));
        return e;
    }

    /** 执行一个场景，随后查询该姓名的实际落库行数并给出结论 */
    private static void check(TxVerifyService tx, String title, String name, Runnable action) {
        System.out.println("------------------------------------------------------------");
        System.out.println("[TEST] " + title);
        action.run();
        long rows = tx.countByName(name);
        System.out.println("       验证 SELECT count(*) WHERE emp_name='" + name + "' = " + rows
                + (rows == 0 ? "  -> 数据已回滚" : "  -> 数据留在库中（事务未生效 / 未回滚）"));
    }
}

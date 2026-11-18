package com.lab.emp;

import com.lab.emp.common.BizException;
import com.lab.emp.common.Result;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.controller.DeptController;
import com.lab.emp.entity.Dept;
import com.lab.emp.entity.Emp;
import com.lab.emp.service.DeptService;
import com.lab.emp.service.EmpService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * 步骤 6（拓展）验证工具：dept 表与 Emp 一对多关联
 * 纯 Service/Controller 直调（DB 行为与 HTTP 层一致，HTTP 包装已在 Step5 验证过）
 */
public class Step6VerifyTool {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext root = new AnnotationConfigApplicationContext();
        root.register(SpringConfig.class);
        root.refresh();

        DeptService deptService = root.getBean(DeptService.class);
        EmpService empService = root.getBean(EmpService.class);
        DeptController deptController = new DeptController();
        // Controller 字段注入在无 MVC 容器的直调场景下手工装配
        inject(deptController, "deptService", deptService);

        // 1. dept 表基础数据（应由 init.sql 从 emp 同步：技术部/市场部/人事部）
        List<Dept> depts = deptService.list();
        System.out.println("[6] dept 表部门数 = " + depts.size() + "（应为 3）");
        depts.forEach(d -> System.out.println("       " + d));

        // 2. 一对多核心：部门 + 名下员工列表
        Dept tech = deptService.lambdaQuery().eq(Dept::getDeptName, "技术部").one();
        Dept withEmps = deptService.getWithEmps(tech.getDeptNo());
        System.out.println("[6] GET /depts/" + tech.getDeptNo() + "/emps -> 技术部名下 "
                + withEmps.getEmpList().size() + " 人: " + withEmps.getEmpList());

        // 3. 反向关联：员工列表嵌套 deptInfo（批量装配，无 N+1）
        List<Emp> emps = empService.listWithDept();
        long filled = emps.stream().filter(e -> e.getDeptInfo() != null).count();
        System.out.println("[6] listWithDept: " + emps.size() + " 名员工，其中 " + filled
                + " 名已关联 deptInfo");
        emps.forEach(e -> System.out.println("       " + e.getEmpName() + " -> "
                + (e.getDeptInfo() != null ? e.getDeptInfo().getDeptName() + "(no="
                + e.getDeptInfo().getDeptNo() + ")" : "无匹配部门")));

        // 4. 级联保护：名下有员工的部门禁止删除
        try {
            deptService.removeDept(tech.getDeptNo());
            System.out.println("[6] 删除技术部 -> 未拦截（错误！）");
        } catch (BizException e) {
            System.out.println("[6] 删除有员工的部门 -> 拦截 code=" + e.getCode() + ", " + e.getMessage());
        }

        // 5. 悬空引用保护：员工指定不存在的部门
        try {
            DeptControllerProbe.rejectUnknownDept(empService, deptService);
            System.out.println("[6] 指定不存在部门 -> 未拦截（错误！）");
        } catch (BizException e) {
            System.out.println("[6] 员工指定不存在部门 -> 拦截 code=" + e.getCode() + ", " + e.getMessage());
        }

        // 6. 空部门的新增/修改不受影响（dept 可空的历史行为保持）
        Emp noDept = new Emp();
        noDept.setEmpName("临时工");
        noDept.setGender("男");
        empService.save(noDept);
        System.out.println("[6] 不填部门的员工可正常新增 id=" + noDept.getEmpId() + "，清理=" + empService.removeById(noDept.getEmpId()));

        // 7. 部门完整生命周期：新增 -> 唯一约束 -> 删除
        Dept finance = new Dept();
        finance.setDeptName("财务部6号验证");
        finance.setDeptDesc("验证用");
        Result<Dept> created = deptController.create(finance);
        System.out.println("[6] POST /depts -> code=" + created.getCode() + ", 回填 deptNo=" + created.getData().getDeptNo());
        Dept dup = new Dept();
        dup.setDeptName("财务部6号验证");
        try {
            deptService.save(dup);
            System.out.println("[6] 重复部门名 -> 未拦截（错误！）");
        } catch (org.springframework.dao.DuplicateKeyException e) {
            System.out.println("[6] 重复部门名 -> uk_dept_name 唯一约束拦截（HTTP 层由 advice 转 409）");
        }
        Result<Boolean> removed = deptController.delete(created.getData().getDeptNo());
        System.out.println("[6] DELETE /depts/" + created.getData().getDeptNo() + "（名下无员工）-> code="
                + removed.getCode() + ", data=" + removed.getData());

        // 8. 数据复位检查
        System.out.println("[6] 最终 dept 数 = " + deptService.count() + "（应为 3），emp 数 = "
                + empService.count() + "（应为 4）");

        root.close();
    }

    /** 复用 EmpController 的私有校验逻辑：反射调用 checkDeptExists */
    static class DeptControllerProbe {
        static void rejectUnknownDept(EmpService empService, DeptService deptService) throws Exception {
            com.lab.emp.controller.EmpController c = new com.lab.emp.controller.EmpController();
            Step6VerifyTool.inject(c, "empService", empService);
            Step6VerifyTool.inject(c, "deptService", deptService);
            Emp e = new Emp();
            e.setEmpName("挂靠测试");
            e.setGender("男");
            e.setDept("火星部");
            try {
                c.create(e);
            } catch (BizException ex) {
                throw ex;
            }
        }
    }

    private static void inject(Object target, String field, Object value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("测试装配失败: " + field, e);
        }
    }
}

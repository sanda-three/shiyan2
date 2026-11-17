package com.lab.emp;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.config.WebMvcConfig;
import com.lab.emp.common.Result;
import com.lab.emp.controller.EmpController;
import com.lab.emp.entity.Emp;
import com.lab.emp.service.EmpService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

/**
 * 步骤 4.2 逐层校验工具：实体 -> Mapper -> Service -> Controller 全链路验证
 */
public class Step42VerifyTool {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext root = new AnnotationConfigApplicationContext();
        root.register(SpringConfig.class);
        root.refresh();

        AnnotationConfigWebApplicationContext mvc = new AnnotationConfigWebApplicationContext();
        mvc.register(WebMvcConfig.class);
        mvc.setServletContext(new MockServletContext());
        mvc.setParent(root);
        mvc.refresh();

        EmpService service = root.getBean(EmpService.class);
        EmpController controller = mvc.getBean(EmpController.class);

        // ===== Service 层：业务方法语义 =====
        List<Emp> tech = service.listByDepartment("技术部");
        System.out.println("[4.2] listByDepartment(技术部) = " + tech.size() + " 条");
        System.out.println("[4.2] searchByEmpName(张) = " + service.searchByEmpName("张").size() + " 条（模糊）");
        Page<Emp> p = service.pageByDepartment(1, 5, null);
        System.out.printf("[4.2] pageByDepartment(1,5) 当前页 %d 条 / 总计 %d 条 / 共 %d 页（分页插件生效）%n",
                p.getRecords().size(), p.getTotal(), p.getPages());
        System.out.println("[4.2] isEmailTaken(lina@lab.com) = " + service.isEmailTaken("lina@lab.com")
                + "，isEmailTaken(new@lab.com) = " + service.isEmailTaken("new@lab.com"));

        // ===== Controller 层：RESTful + Result 统一返回 =====
        Result<Emp> found = controller.getById(1);
        System.out.println("[4.2] GET /api/emps/1 -> code=" + found.getCode() + ", " + found.getData().getEmpName());
        Result<Emp> missing = controller.getById(999);
        System.out.println("[4.2] GET /api/emps/999 -> code=" + missing.getCode() + "（404 语义）");

        // POST 冲突校验（uk_email 唯一约束前置检查）
        Emp dup = newEmp("测试甲", "测试部", "new@lab.com");
        dup.setEmail("lina@lab.com");
        System.out.println("[4.2] POST 重复邮箱 -> code=" + controller.create(dup).getCode() + "（400 拒绝）");

        // POST 正常新增（回填自增主键）-> PUT 更新 -> DELETE 删除，全生命周期
        Emp fresh = newEmp("测试乙", "测试部", "step42test@lab.com");
        Result<Emp> created = controller.create(fresh);
        Integer newId = created.getData().getEmpId();
        System.out.println("[4.2] POST 新增 -> code=" + created.getCode() + ", 回填主键 id=" + newId);
        Emp patch = new Emp();
        patch.setSalary(new BigDecimal("7777.77"));
        System.out.println("[4.2] PUT /api/emps/" + newId + " -> code=" + controller.update(newId, patch).getCode()
                + "，更新后薪资=" + service.getById(newId).getSalary());
        System.out.println("[4.2] DELETE /api/emps/" + newId + " -> code=" + controller.delete(newId).getCode()
                + "，删除后再查 -> code=" + controller.getById(newId).getCode());

        // 数据复位核对（新增即删除，应回到 8 条）
        System.out.println("[4.2] 最终总记录数 = " + service.count() + "（应为 8）");

        mvc.close();
        root.close();
    }

    private static Emp newEmp(String name, String dept, String email) {
        Emp e = new Emp();
        e.setEmpName(name);
        e.setGender("男");
        e.setDepartment(dept);
        e.setJobTitle("临时测试岗");
        e.setEmail(email);
        e.setSalary(new BigDecimal("1000.00"));
        return e;
    }
}

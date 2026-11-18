package com.lab.emp;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.config.WebMvcConfig;
import com.lab.emp.common.Result;
import com.lab.emp.controller.EmpController;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.entity.Dept;
import com.lab.emp.entity.Emp;
import com.lab.emp.service.DeptService;
import com.lab.emp.service.EmpService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

/**
 * 步骤 4.2/5.1 逐层校验工具（适配 ssm_emp 表结构）：Service -> Controller 直调验证
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
        DeptService deptService = root.getBean(DeptService.class);
        EmpController controller = mvc.getBean(EmpController.class);

        // 一对多拓展后，员工新增会校验部门必须存在于 dept 表（见 EmpController.checkDeptExists），
        // 故先建一个仅供本次验证使用的部门，验证结束再删除，保持原始数据不变
        Dept testDept = new Dept();
        testDept.setDeptName("测试部");
        testDept.setDeptDesc("4.2 验证用临时部门");
        deptService.save(testDept);
        System.out.println("[准备] 新建临时部门 测试部 deptNo=" + testDept.getDeptNo());

        // ===== Service 层（步骤 5.1） =====
        // 查询条件封装为 EmpQueryDTO（任务 5.1 要求），空条件即全量分页
        Page<Emp> p = service.pageQuery(1, 2, new EmpQueryDTO());
        System.out.printf("[5.1] pageQuery 无参分页：当前页 %d 条 / 总计 %d 条 / 共 %d 页%n",
                p.getRecords().size(), p.getTotal(), p.getPages());
        EmpQueryDTO cond = new EmpQueryDTO();
        cond.setName("张");
        cond.setGender("男");
        cond.setDept("技术部");
        Page<Emp> filtered = service.pageQuery(1, 10, cond);
        System.out.println("[5.1] pageQuery(name=张/gender=男/dept=技术部) = " + filtered.getTotal() + " 条："
                + (filtered.getTotal() > 0 ? filtered.getRecords().get(0).getEmpName() : "无"));
        System.out.println("[5.1] listByDept(技术部) = " + service.listByDept("技术部").size() + " 条");

        // ===== Controller 层（步骤 5.2）全生命周期 =====
        Result<Emp> found = controller.getById(1);
        System.out.println("[5.2] GET /emps/1 -> code=" + found.getCode() + ", " + found.getData().getEmpName());
        System.out.println("[5.2] GET /emps/999 -> code=" + controller.getById(999).getCode());

        Emp fresh = new Emp();
        fresh.setEmpName("临时工");
        fresh.setGender("男");
        fresh.setDept("测试部");
        fresh.setPost("测试岗");
        fresh.setSalary(new BigDecimal("6666.00"));
        Result<Emp> created = controller.create(fresh);
        Integer newId = created.getData().getEmpId();
        System.out.println("[5.2] POST /emps -> code=" + created.getCode() + ", 回填 id=" + newId
                + ", status(数据库默认)=" + service.getById(newId).getStatus());

        fresh.setEmpId(newId);
        fresh.setSalary(new BigDecimal("8888.88"));
        System.out.println("[5.2] PUT /emps -> code=" + controller.update(fresh).getCode()
                + "，更新后薪资=" + service.getById(newId).getSalary());

        System.out.println("[5.2] DELETE /emps 批量 [" + newId + ", 不存在id...] -> code="
                + controller.deleteBatch(List.of(newId, 9999)).getCode());
        System.out.println("[5.2] 删除后再查 id=" + newId + " -> code=" + controller.getById(newId).getCode());

        // 数据复位核对（ssm_emp 原始 4 条）
        System.out.println("[5.2] 最终总记录数 = " + service.count() + "（应为 4）");
        deptService.removeById(testDept.getDeptNo());
        System.out.println("[清理] 删除临时部门 测试部，dept 表部门数 = " + deptService.count() + "（应为 3）");

        mvc.close();
        root.close();
    }
}

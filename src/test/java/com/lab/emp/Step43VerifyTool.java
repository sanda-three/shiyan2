package com.lab.emp;

import com.lab.emp.common.BizException;
import com.lab.emp.common.GlobalExceptionHandler;
import com.lab.emp.common.Result;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.config.WebMvcConfig;
import com.lab.emp.controller.EmpController;
import com.lab.emp.entity.Emp;
import com.lab.emp.service.EmpService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.method.ControllerAdviceBean;

import java.math.BigDecimal;
import java.util.List;

/**
 * 步骤 4.3 验证工具：统一返回结果封装
 * 1. GlobalExceptionHandler 能被 MVC 子容器识别为 @ControllerAdvice；
 * 2. 三类异常均转换为 Result（BizException/唯一键冲突/未知异常）；
 * 3. 真实触发 uk_email 冲突（绕过 Controller 前置校验直接 save），异常路径闭环。
 */
public class Step43VerifyTool {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext root = new AnnotationConfigApplicationContext();
        root.register(SpringConfig.class);
        root.refresh();

        AnnotationConfigWebApplicationContext mvc = new AnnotationConfigWebApplicationContext();
        mvc.register(WebMvcConfig.class);
        mvc.setServletContext(new MockServletContext());
        mvc.setParent(root);
        mvc.refresh();

        // 1. Advice 是否在 MVC 容器（含父容器层级）中被发现
        List<ControllerAdviceBean> advices = ControllerAdviceBean.findAnnotatedBeans(mvc);
        boolean found = advices.stream().anyMatch(b ->
                GlobalExceptionHandler.class.equals(b.getBeanType()));
        System.out.println("[4.3] GlobalExceptionHandler 被 MVC 容器识别 = " + found);

        // 2. 三类异常 -> Result 映射
        GlobalExceptionHandler handler = mvc.getBean(GlobalExceptionHandler.class);
        Result<Void> biz = handler.handleBiz(new BizException(400, "参数不完整"));
        Result<Void> dup = handler.handleDuplicateKey(new DuplicateKeyException("uk_email"));
        Result<Void> oops = handler.handleOther(new IllegalStateException());
        System.out.printf("[4.3] BizException -> code=%d, msg=%s%n", biz.getCode(), biz.getMessage());
        System.out.printf("[4.3] DuplicateKey -> code=%d, msg=%s%n", dup.getCode(), dup.getMessage());
        System.out.printf("[4.3] 未知异常    -> code=%d, msg=%s%n", oops.getCode(), oops.getMessage());

        // 3. 真实唯一键冲突：绕过 Controller 的 isEmailTaken 前置校验直接插入重复邮箱
        EmpService service = root.getBean(EmpService.class);
        Emp dupEmp = new Emp();
        dupEmp.setEmpName("冲突测试");
        dupEmp.setDepartment("测试部");
        dupEmp.setEmail("lina@lab.com"); // 已存在
        dupEmp.setSalary(new BigDecimal("1.00"));
        try {
            service.save(dupEmp);
            System.out.println("[4.3] 未触发冲突（不符合预期！）");
        } catch (DuplicateKeyException e) {
            System.out.println("[4.3] 数据库层真实冲突已捕获 -> " + handler.handleDuplicateKey(e).getCode());
        }

        // 正常路径仍返回统一结构
        Result<Emp> ok = mvc.getBean(EmpController.class).getById(1);
        System.out.println("[4.3] 正常路径 -> code=" + ok.getCode() + ", data=" + ok.getData().getEmpName());

        // 复位核对（冲突插入被数据库拒绝，不应产生新行）
        System.out.println("[4.3] 总记录数 = " + service.count() + "（应为 8）");

        mvc.close();
        root.close();
    }
}

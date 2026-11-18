package com.lab.emp;

import com.lab.emp.common.BizException;
import com.lab.emp.common.GlobalExceptionHandler;
import com.lab.emp.common.Result;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.config.WebMvcConfig;
import com.lab.emp.controller.EmpController;
import com.lab.emp.entity.Emp;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.method.ControllerAdviceBean;

import java.util.List;

/**
 * 步骤 4.3 验证工具（适配 ssm_emp，无唯一键表）：
 * 1. GlobalExceptionHandler 被 MVC 容器识别为 @ControllerAdvice；
 * 2. 各类异常 -> 统一 Result 映射（业务/冲突/校验/请求体/未知）。
 * HTTP 层的真实触发见 Step5VerifyTool（MockMvc）。
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

        List<ControllerAdviceBean> advices = ControllerAdviceBean.findAnnotatedBeans(mvc);
        boolean found = advices.stream().anyMatch(b ->
                GlobalExceptionHandler.class.equals(b.getBeanType()));
        System.out.println("[4.3] GlobalExceptionHandler 被 MVC 容器识别 = " + found);

        GlobalExceptionHandler handler = mvc.getBean(GlobalExceptionHandler.class);
        Result<Void> biz = handler.handleBiz(new BizException(400, "修改操作必须携带 empId"));
        Result<Void> dup = handler.handleDuplicateKey(new DuplicateKeyException("uk"));
        Result<Void> oops = handler.handleOther(new IllegalStateException());
        System.out.printf("[4.3] BizException -> code=%d, msg=%s%n", biz.getCode(), biz.getMessage());
        System.out.printf("[4.3] DuplicateKey -> code=%d%n", dup.getCode());
        System.out.printf("[4.3] 未知异常     -> code=%d%n", oops.getCode());

        // Controller 直调的业务分支
        EmpController controller = mvc.getBean(EmpController.class);
        System.out.println("[4.3] 查询不存在员工 -> code=" + controller.getById(999).getCode());
        Emp noId = new Emp();
        noId.setEmpName("无主键更新");
        try {
            controller.update(noId);
            System.out.println("[4.3] 未触发 empId 校验（不符合预期！）");
        } catch (BizException e) {
            System.out.println("[4.3] PUT 缺 empId -> 抛出 BizException code=" + e.getCode()
                    + "（HTTP 层由 advice 转 Result，见 Step5）");
        }

        mvc.close();
        root.close();
    }
}

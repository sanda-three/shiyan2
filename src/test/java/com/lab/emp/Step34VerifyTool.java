package com.lab.emp;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.config.WebMvcConfig;
import com.lab.emp.controller.EmpController;
import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.EmpMapper;
import com.lab.emp.service.EmpService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * 步骤 3.4：整合验证工具（不依赖 Tomcat，编程式启动与 Web 容器等价的上下文层级）
 * 1. 根容器 = ContextLoaderListener + SpringConfig
 * 2. MVC 子容器 = DispatcherServlet + WebMvcConfig（父容器为根容器）
 * 3. 调用 Service -> MP selectById 验证数据库连通与事务管理器
 */
public class Step34VerifyTool {

    public static void main(String[] args) {
        // ========== 1. 根容器启动（等价于 ContextLoaderListener 加载 SpringConfig） ==========
        AnnotationConfigApplicationContext root = new AnnotationConfigApplicationContext();
        root.register(SpringConfig.class);
        root.refresh();
        System.out.println("[3.4] 根容器启动成功，无 Bean 注入异常");

        // Bean 必要性核对
        DruidDataSource ds = root.getBean(DruidDataSource.class);
        System.out.println("  - DataSource: DruidDataSource, url = " + ds.getUrl());
        SqlSessionFactory sqlSessionFactory = root.getBean(SqlSessionFactory.class);
        System.out.println("  - SqlSessionFactory: " + sqlSessionFactory.getClass().getSimpleName());
        boolean pagePlugin = sqlSessionFactory.getConfiguration().getInterceptors().stream()
                .anyMatch(i -> i instanceof MybatisPlusInterceptor);
        System.out.println("  - 分页插件已注册 = " + pagePlugin);
        System.out.println("  - MapperScanner 注册的 EmpMapper = " + root.getBeanNamesForType(EmpMapper.class).length + " 个");
        System.out.println("  - 事务管理器 = " + root.getBean(DataSourceTransactionManager.class).getClass().getSimpleName());
        System.out.println("  - Controller 未进入根容器（被 excludeFilters 排除）= "
                + (root.getBeanNamesForType(EmpController.class).length == 0));

        // ========== 2. Service -> MP selectById（走 @Transactional 切面，验证连通与事务装配） ==========
        EmpService service = root.getBean(EmpService.class);
        Emp emp = service.getById(1);
        System.out.println("[3.4] selectById(1) 结果: " + emp);
        EmpMapper mapper = root.getBean(EmpMapper.class);
        System.out.println("[3.4] MP 通用 selectCount(null) 总记录数 = " + mapper.selectCount(null));

        // ========== 3. MVC 子容器启动（等价于 DispatcherServlet 加载 WebMvcConfig） ==========
        AnnotationConfigWebApplicationContext mvc = new AnnotationConfigWebApplicationContext();
        mvc.register(WebMvcConfig.class);
        // 脱离 Tomcat 验证时需手动注入 MockServletContext（真实容器中由 DispatcherServlet 设置）
        mvc.setServletContext(new MockServletContext());
        mvc.setParent(root);          // 子容器可引用根容器中的 Service
        mvc.refresh();
        System.out.println("[3.4] MVC 子容器启动成功");
        System.out.println("  - EmpController = " + (mvc.getBean(EmpController.class) != null));
        System.out.println("  - 视图解析器 = " + mvc.getBean(InternalResourceViewResolver.class).getClass().getSimpleName());
        // 子容器经父容器引用注入 Service，验证跨容器装配
        mvc.getBean(EmpController.class).getById(2);
        System.out.println("[3.4] 子容器 Controller 调用父容器 Service 成功");

        // ========== 4. 关闭，释放 Druid 连接池 ==========
        mvc.close();
        root.close();
        System.out.println("[3.4] 容器正常关闭，验证全部通过");
    }
}

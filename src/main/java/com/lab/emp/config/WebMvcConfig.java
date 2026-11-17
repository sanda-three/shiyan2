package com.lab.emp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * 步骤 3.3：SpringMVC 配置（DispatcherServlet 子容器）
 * - @EnableWebMvc：启用 MVC 注解驱动（Jackson JSON 转换、@GetMapping 路由等）；
 * - 扫描 controller 与 common（含 GlobalExceptionHandler）：Service/数据访问 Bean 属于根容器，避免重复实例化；
 * - 视图解析器：逻辑视图名 -> /WEB-INF/views/*.jsp。
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.lab.emp.controller", "com.lab.emp.common"})
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }
}

package com.lab.emp.config;

import com.lab.emp.aspect.WebLogAspect;
import com.lab.emp.interceptor.LoginInterceptor;
import com.lab.emp.interceptor.RequestLogInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * 步骤 3.3：SpringMVC 配置（DispatcherServlet 子容器）
 * - @EnableWebMvc：启用 MVC 注解驱动（Jackson JSON 转换、@GetMapping 路由等）；
 * - @EnableAspectJAutoProxy：在本（子）容器开启 AOP 自动代理，使 WebLogAspect
 *   能增强同样位于子容器的 Controller，详见 WebLogAspect 的父子容器说明；
 * - 扫描 controller 与 common（含 GlobalExceptionHandler）：Service/数据访问 Bean 属于根容器，避免重复实例化；
 * - 视图解析器：逻辑视图名 -> /WEB-INF/views/*.jsp。
 */
@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.lab.emp.controller", "com.lab.emp.common"})
public class WebMvcConfig implements WebMvcConfigurer {

    /** 步骤 6.4：请求日志切面。注册在子容器，与它要增强的 Controller 同容器才生效 */
    @Bean
    public WebLogAspect webLogAspect() {
        return new WebLogAspect();
    }

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    /**
     * 步骤 3.3：静态资源放行。
     * DispatcherServlet 映射在 "/"，会吞掉所有请求；开启默认 Servlet 处理后，
     * /css、/js、/images 这类静态文件仍由容器 DefaultServlet 返回，
     * 否则前端页面直接 404。
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /**
     * 步骤 3.3：全局跨域配置，适配前后端分离。
     * 用了 allowCredentials(true)（允许携带 Cookie）就不能把 allowedOrigins 写成 "*"，
     * 必须用 allowedOriginPatterns 的模式匹配，这是 Spring 5.3 起的硬性规定。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 拦截器注册（执行顺序按注册顺序）：
     * 1. 请求日志拦截全部路径（含登录，便于审计）
     * 2. 登录拦截全部业务路径，排除 /login；/error 为容器内部转发也排除
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLogInterceptor())
                .addPathPatterns("/**");
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/error");
    }
}

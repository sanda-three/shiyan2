package com.lab.emp.config;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import jakarta.servlet.Filter;

/**
 * 步骤 3.1：Web 初始化配置（替代 web.xml）
 * 实现 WebApplicationInitializer 接口后，Servlet 3.0+ 容器启动时自动发现本类，
 * 完成监听器、DispatcherServlet、过滤器的注册，效果与原 web.xml 等价：
 * - 根容器：SpringConfig（数据源 / MyBatis-Plus / 事务）
 * - MVC 子容器：WebMvcConfig
 * - 过滤器：字符编码 + PUT/DELETE 表单方法转换
 * 说明：web.xml 已彻底删除，maven-war-plugin 的 failOnMissingWebXml
 * 校验已相应关闭，项目不再依赖任何 XML 描述符。
 */
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    /** 根容器（ContextLoaderListener）加载的配置类 */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{SpringConfig.class};
    }

    /** DispatcherServlet 子容器加载的配置类 */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebMvcConfig.class};
    }

    /** 前端控制器拦截全部请求，对应原 web.xml 的 "/" */
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    /** 注册到 DispatcherServlet 上的过滤器，对应原 web.xml 的两个 filter */
    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setForceRequestEncoding(true);
        encodingFilter.setForceResponseEncoding(true);
        return new Filter[]{encodingFilter, new HiddenHttpMethodFilter()};
    }
}

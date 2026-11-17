package com.lab.emp.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 步骤 3.2：Spring 根容器配置（数据源、MyBatis-Plus、事务）
 * 说明：
 * - SqlSessionFactory 采用 MP 官方的 MybatisSqlSessionFactoryBean（步骤说明允许的写法）。
 *   若用示例中的原生 SqlSessionFactoryBean，BaseMapper 的 selectById 等通用方法
 *   无法注入（Invalid bound statement），因为 MP 通用方法需要 MybatisSqlSessionFactory
 *   在构建时注册；
 * - 连接四要素全部来自 jdbc.properties，禁止硬编码；
 * - Druid 连接池参数同样从属性文件读取（druid.initialSize / druid.maxActive）。
 */
@Configuration
@ComponentScan(basePackages = "com.lab.emp",
        excludeFilters = {
                // Controller 归 DispatcherServlet 子容器，避免重复实例化
                @ComponentScan.Filter(type = FilterType.ANNOTATION,
                        classes = org.springframework.stereotype.Controller.class),
                // MVC 配置类与全局异常处理器归 DispatcherServlet 子容器，
                // 在根容器扫描中排除，避免重复注册
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = {WebMvcConfig.class, com.lab.emp.common.GlobalExceptionHandler.class})
        })
@PropertySource("classpath:jdbc.properties")
@EnableTransactionManagement
public class SpringConfig {

    @Bean
    public DataSource dataSource(
            @Value("${jdbc.driver}") String driver,
            @Value("${jdbc.url}") String url,
            @Value("${jdbc.username}") String user,
            @Value("${jdbc.password}") String pwd,
            @Value("${druid.initialSize}") int initialSize,
            @Value("${druid.maxActive}") int maxActive) {
        DruidDataSource ds = new DruidDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(url);
        ds.setUsername(user);
        ds.setPassword(pwd);
        ds.setInitialSize(initialSize);
        ds.setMaxActive(maxActive);
        return ds;
    }

    @Bean
    public MybatisSqlSessionFactoryBean sqlSessionFactory(DataSource ds) throws Exception {
        MybatisSqlSessionFactoryBean fb = new MybatisSqlSessionFactoryBean();
        fb.setDataSource(ds);
        // 实体类别名包（MP 实体）
        fb.setTypeAliasesPackage("com.lab.emp.entity");
        // 注册 MyBatis-Plus 分页插件
        MybatisPlusInterceptor mp = new MybatisPlusInterceptor();
        mp.addInnerInterceptor(new PaginationInnerInterceptor());
        fb.setPlugins(mp);
        // 开启驼峰映射（使用 MP 提供的 MybatisConfiguration）
        MybatisConfiguration cfg = new MybatisConfiguration();
        cfg.setMapUnderscoreToCamelCase(true);
        fb.setConfiguration(cfg);
        return fb;
    }

    @Bean
    // static 声明：MapperScannerConfigurer 是 BeanDefinitionRegistryPostProcessor，
    // 非 static 会导致 SpringConfig 在容器启动早期被实例化并产生增强警告
    public static MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer cfg = new MapperScannerConfigurer();
        cfg.setBasePackage("com.lab.emp.mapper");
        return cfg;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }
}

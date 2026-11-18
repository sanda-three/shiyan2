package com.lab.emp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 步骤 6.4：AOP 请求日志切面。
 *
 * <p>环绕通知统一记录目标方法的签名、入参、耗时与异常，
 * 业务代码里不用到处写日志语句，这是"横切关注点用 AOP"的典型场景。
 *
 * <p>【父子容器要点】本类刻意不加 {@code @Component}，而是在
 * {@link com.lab.emp.config.WebMvcConfig} 中用 {@code @Bean} 注册：
 * Controller 由 DispatcherServlet 的<b>子容器</b>创建，而根容器
 * （{@link com.lab.emp.config.SpringConfig}）扫描到的切面 Bean 只会给根容器的
 * Bean 建代理，对子容器里的 Controller 无效——切面若注册在根容器，
 * 日志一行都不会打印。所以切面必须和它要增强的类在同一个容器里。
 * 这也正是思考题第 4 问"拦截器与 AOP 执行时机差异"的工程前提。
 *
 * <p>与 {@link com.lab.emp.interceptor.RequestLogInterceptor} 的分工：
 * 拦截器拿到的是 HTTP 语义（方法名、URI、状态码），切面拿到的是 Java 语义
 * （目标方法签名与实参值）。两者互补，本实验同时保留以便对比。
 */
@Aspect
public class WebLogAspect {

    private static final Logger log = LoggerFactory.getLogger(WebLogAspect.class);

    /** 切点：表现层所有类的所有公共方法。execution 表达式按包名匹配 */
    @Pointcut("execution(* com.lab.emp.controller..*(..))")
    public void pt() {
    }

    /**
     * 环绕通知：必须调用 pjp.proceed() 才会真正执行目标方法，
     * 且异常要原样抛出，否则会破坏 GlobalExceptionHandler 的兜底逻辑。
     */
    @Around("pt()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String method = pjp.getSignature().toShortString();
        log.info("[AOP REQ ] {} 参数：{}", method, Arrays.toString(pjp.getArgs()));
        try {
            Object result = pjp.proceed();
            log.info("[AOP RESP] {} 耗时：{} ms", method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            log.error("[AOP ERR ] {} 异常：{}", method, t.getMessage());
            throw t;
        }
    }
}

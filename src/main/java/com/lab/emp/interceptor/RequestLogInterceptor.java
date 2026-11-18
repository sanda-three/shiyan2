package com.lab.emp.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求日志拦截器：记录方法、URI、耗时与状态码，输出到 log4j
 */
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLogInterceptor.class);

    /** afterCompletion 计算耗时用的起始时间属性名 */
    private static final String START_ATTR = "reqStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_ATTR, System.currentTimeMillis());
        log.info("[REQ ] {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long start = (Long) request.getAttribute(START_ATTR);
        log.info("[RESP] {} {} -> {} ({} ms){}", request.getMethod(), request.getRequestURI(),
                response.getStatus(), System.currentTimeMillis() - start,
                ex != null ? " exception=" + ex.getClass().getSimpleName() : "");
    }
}

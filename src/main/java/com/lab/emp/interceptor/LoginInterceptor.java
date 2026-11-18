package com.lab.emp.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录权限拦截器：未登录（session 无 loginUser）的请求直接返回 401 统一 Result JSON，
 * 登录接口 /login 在 WebMvcConfig 中被排除，不拦截
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // CORS 预检请求放行：Spring MVC 处理预检时仍会执行原有拦截器链，
        // 若在此返回 401，浏览器会判定预检失败从而拦截后续真实请求（跨域前端无法联调）。
        // 预检只携带 Origin/Access-Control-* 头、不含业务语义，交由 CorsInterceptor 应答即可。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            return true; // 已登录放行
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或会话已过期，请先 POST /login\",\"data\":null}");
        return false;
    }
}

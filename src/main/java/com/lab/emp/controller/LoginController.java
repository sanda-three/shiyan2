package com.lab.emp.controller;

import com.lab.emp.common.Result;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器（配合 LoginInterceptor 的会话认证入口）
 * 实验演示用固定账号；生产应查用户表 + 密码散列，作为后续拓展
 */
@RestController
public class LoginController {

    /** 演示账号（实验用，不入库） */
    private static final String DEMO_USER = "admin";
    private static final String DEMO_PWD = "admin123";

    /** 登录：POST /login?username=admin&password=admin123（成功后 sessionId 即凭证） */
    @PostMapping("/login")
    public Result<String> login(@RequestParam("username") String username,
                                @RequestParam("password") String password,
                                HttpSession session) {
        if (DEMO_USER.equals(username) && DEMO_PWD.equals(password)) {
            session.setAttribute("loginUser", username);
            return Result.ok("登录成功，用户: " + username);
        }
        return Result.fail(401, "用户名或密码错误");
    }

    /** 退出登录：POST /logout */
    @PostMapping("/logout")
    public Result<String> logout(HttpSession session) {
        session.invalidate();
        return Result.ok("已退出登录");
    }
}

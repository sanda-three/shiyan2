package com.lab.emp;

import com.lab.emp.config.SpringConfig;
import com.lab.emp.config.WebMvcConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 步骤 5 全链路验证工具：用 MockMvc（webAppContextSetup 加载真实 WebMvcConfig，
 * 含 @EnableWebMvc 完整参数解析与拦截器 excludePathPatterns 规则）模拟 HTTP 请求，
 * 依次经过 拦截器 -> 参数校验 -> Controller -> Service -> 数据库 -> Result JSON，
 * 与 Tomcat + ApiFox 的调试路径等价。
 * 会话模拟 ApiFox 流程：先 /login 拿到 session，后续业务请求携带同一 session，
 * /logout 后原凭证失效。
 */
public class Step5VerifyTool {

    static AnnotationConfigWebApplicationContext mvc;
    static MockMvc client;

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext root = new AnnotationConfigApplicationContext();
        root.register(SpringConfig.class);
        root.refresh();

        mvc = new AnnotationConfigWebApplicationContext();
        mvc.register(WebMvcConfig.class);
        mvc.setServletContext(new MockServletContext());
        mvc.setParent(root);
        mvc.refresh();

        // 真实子容器：DispatcherServlet 走 WebMvcConfig 的完整 MVC 栈
        client = MockMvcBuilders.webAppContextSetup(mvc).build();

        // 1. 未登录访问业务接口 -> 401
        show("未登录 GET /emps", get("/emps"), null);
        // 2. 错误密码登录 -> code=401（/login 在放行名单，可直达 Controller）
        show("错误密码 POST /login", post("/login")
                .param("username", "admin").param("password", "bad"), new MockHttpSession());
        // 3. 正确登录 -> 200，此后业务请求携带该 session（等价 ApiFox 自动保留 Cookie）
        MockHttpSession auth = new MockHttpSession();
        show("正确登录 POST /login", post("/login")
                .param("username", "admin").param("password", "admin123"), auth);

        // 4. 分页 + 多条件
        show("GET /emps?page=1&size=2", get("/emps").param("page", "1").param("size", "2"), auth);
        show("GET /emps?name=张&gender=男&dept=技术部", get("/emps")
                .param("name", "张").param("gender", "男").param("dept", "技术部"), auth);
        show("GET /emps/1", get("/emps/1"), auth);
        show("GET /emps/999", get("/emps/999"), auth);

        // 5. 参数校验（@Valid）：姓名空 + 性别非法
        show("POST 非法员工", json(post("/emps"), "{\"empName\":\"\",\"gender\":\"中\"}"), auth);
        // 5b. 一对多拓展：悬空引用拦截（部门不存在）、空部门不校验
        show("POST 悬空部门员工", json(post("/emps"),
                "{\"empName\":\"无底人\",\"gender\":\"男\",\"dept\":\"不存在部\"}"), auth);
        // 5c. 先建部门（POST /depts），才能挂同名部门员工
        String deptBody = show("POST /depts 新建测试部", json(post("/depts"),
                "{\"deptName\":\"测试部\",\"deptDesc\":\"ApiFox验证\"}"), auth);
        Matcher dm = Pattern.compile("\"deptNo\":(\\d+)").matcher(deptBody);
        int deptNo = dm.find() ? Integer.parseInt(dm.group(1)) : -1;
        // 6. 正常新增（部门已存在）-> 回填主键
        String body = show("POST 合法新增", json(post("/emps"),
                "{\"empName\":\"ApiFox测试\",\"gender\":\"男\",\"dept\":\"测试部\","
                        + "\"post\":\"测试岗\",\"salary\":5000,\"hireDate\":\"2026-01-01\"}"), auth);
        Matcher m = Pattern.compile("\"empId\":(\\d+)").matcher(body);
        int id = m.find() ? Integer.parseInt(m.group(1)) : -1;
        // 6b. 一对多查询：员工嵌套 deptInfo + 部门名下员工
        show("GET /emps/with-dept", get("/emps/with-dept"), auth);
        show("GET /depts/" + deptNo + "/emps", get("/depts/" + deptNo + "/emps"), auth);
        // 6c. 级联保护：测试部刚挂了员工，删除应被 400 拒绝
        show("DELETE /depts/" + deptNo + "（名下有员工）", delete("/depts/" + deptNo), auth);

        // 7. 修改（完整字段，@Valid 对 PUT 同样生效）/ 单删 / 批删
        show("PUT /emps (id=" + id + ")", json(put("/emps"),
                "{\"empId\":" + id + ",\"empName\":\"ApiFox测试\",\"gender\":\"男\","
                        + "\"dept\":\"测试部\",\"salary\":6000.5}"), auth);
        show("PUT 缺 empId", json(put("/emps"),
                "{\"empName\":\"无主键\",\"gender\":\"男\",\"salary\":1}"), auth);
        show("DELETE /emps/" + id, delete("/emps/" + id), auth);
        show("DELETE /emps 批量(不存在的id)", json(delete("/emps"), "[" + (id + 100000) + "]"), auth);
        show("DELETE /emps 空数组", json(delete("/emps"), "[]"), auth);
        // 7b. 员工已清，此时删除测试部应成功（保证数据复位）
        show("DELETE /depts/" + deptNo + "（名下无员工）", delete("/depts/" + deptNo), auth);

        // 8. 退出后凭证失效（MockHttpSession 允许 invalidate 后继续读，
        // 真实容器中 getSession(false) 会返回新会话，这里直接换用无效会话模拟）
        show("POST /logout", post("/logout"), auth);
        show("退出后 GET /emps", get("/emps"), auth);

        mvc.close();
        root.close();
    }

    /** 执行请求并打印 HTTP 状态码与响应体；session 为 null 表示不携带会话（模拟未登录） */
    private static String show(String title, MockHttpServletRequestBuilder rb, MockHttpSession session)
            throws Exception {
        // 中文参数按 UTF-8 解析（对齐 Tomcat 8+ 默认行为）
        rb.characterEncoding("UTF-8");
        if (session != null) {
            rb.session(session);
        }
        MvcResult result = client.perform(rb).andReturn();
        String resp = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println("[5] " + title + " -> HTTP " + result.getResponse().getStatus()
                + " | " + abbreviate(resp));
        return resp;
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder rb, String body) {
        return rb.contentType("application/json;charset=UTF-8").content(body);
    }

    private static String abbreviate(String s) {
        s = s.replaceAll("\\s+", "");
        return s.length() > 160 ? s.substring(0, 160) + "..." : s;
    }
}

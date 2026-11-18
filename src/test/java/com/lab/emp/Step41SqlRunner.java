package com.lab.emp;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * 临时工具：执行 sql/init.sql（步骤 4.1 + dept 一对多拓展），并输出表结构与索引校验结果
 * <p>
 * 连接信息一律从 classpath 的 jdbc.properties 读取（该文件已 gitignore，仓库内只有
 * jdbc.properties.example 模板），与 SpringConfig 使用同一份配置——
 * 实验要求"禁止硬编码连接四要素"，测试工具同样不能例外，否则真实密码会随源码入库。
 */
public class Step41SqlRunner {

    public static void main(String[] args) throws Exception {
        Properties p = new Properties();
        try (InputStream in = Step41SqlRunner.class.getClassLoader()
                .getResourceAsStream("jdbc.properties")) {
            if (in == null) {
                throw new IllegalStateException("缺少 jdbc.properties，请由 jdbc.properties.example 复制并填写");
            }
            p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        }
        String url = p.getProperty("jdbc.url");
        String script = new String(Files.readAllBytes(
                Paths.get("sql/init.sql")), StandardCharsets.UTF_8);

        try (Connection conn = DriverManager.getConnection(
                url, p.getProperty("jdbc.username"), p.getProperty("jdbc.password"));
             Statement st = conn.createStatement()) {
            // 按括号深度为 0 的分号切分语句（INSERT ... SELECT (...); ... 内嵌分号不截断），跳过字符串外的注释行
            for (String sql : splitStatements(script)) {
                if (sql.trim().equalsIgnoreCase("USE ssm_emp")) {
                    continue;
                }
                if (!sql.trim().regionMatches(true, 0, "CREATE DATABASE", 0, 15)) {
                    st.execute("USE `ssm_emp`");
                    // 字面量示例数据插入前检查表内是否已有数据，避免重复执行时追加；
                    // 部门同步用 INSERT ... SELECT + NOT IN，本身幂等，不需跳过
                    if (sql.trim().regionMatches(true, 0, "INSERT", 0, 6)
                            && !sql.trim().regionMatches(true, 0, "INSERT IGNORE INTO dept", 0, 21)) {
                        try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM emp")) {
                            rs.next();
                            if (rs.getInt(1) > 0) {
                                System.out.println("[SKIP] emp 表已有 " + rs.getInt(1) + " 条数据，跳过示例插入");
                                continue;
                            }
                        }
                    }
                }
                st.execute(sql);
                String[] words = sql.trim().split("\\s+");
                System.out.println("[OK] " + words[0] + " " + (words.length > 1 ? words[1] : ""));
            }
            // 校验：两表索引与约束
            for (String table : new String[]{"emp", "dept"}) {
                try (ResultSet rs = st.executeQuery("SHOW INDEX FROM " + table)) {
                    System.out.println("\n=== " + table + " 索引 ===");
                    while (rs.next()) {
                        System.out.printf("  %s -> %s (%s)%n", rs.getString("Key_name"),
                                rs.getString("Column_name"),
                                rs.getString("Non_unique").equals("0") ? "唯一" : "普通");
                    }
                }
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    rs.next();
                    System.out.println("=== " + table + " 数据行数 = " + rs.getInt(1) + " ===");
                }
            }
        }
    }

    /** 在括号深度 0 且不在单引号内的位置按分号切分 SQL 脚本 */
    private static java.util.List<String> splitStatements(String script) {
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;
        boolean inStr = false;
        for (char ch : script.replaceAll("(?m)^\\s*--.*$", "").toCharArray()) {
            if (ch == '\'' ) {
                inStr = !inStr;
            } else if (!inStr && ch == '(') {
                depth++;
            } else if (!inStr && ch == ')') {
                depth--;
            } else if (!inStr && ch == ';' && depth == 0) {
                if (!cur.toString().trim().isEmpty()) {
                    out.add(cur.toString());
                }
                cur.setLength(0);
                continue;
            }
            cur.append(ch);
        }
        if (!cur.toString().trim().isEmpty()) {
            out.add(cur.toString());
        }
        return out;
    }
}

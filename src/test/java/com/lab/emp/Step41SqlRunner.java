package com.lab.emp;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/** 临时工具：执行 sql/init.sql（步骤 4.1），并输出表结构与索引校验结果 */
public class Step41SqlRunner {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306?useUnicode=true&characterEncoding=utf8"
                + "&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        String script = new String(Files.readAllBytes(
                Paths.get("sql/init.sql")), StandardCharsets.UTF_8);

        try (Connection conn = DriverManager.getConnection(url, "root", "key20051005");
             Statement st = conn.createStatement()) {
            for (String sql : script.replaceAll("(?m)^\\s*--.*$", "").split(";")) {
                if (sql.trim().isEmpty() || sql.trim().equalsIgnoreCase("USE emp_db")) {
                    continue;
                }
                if (!sql.trim().regionMatches(true, 0, "CREATE DATABASE", 0, 15)) {
                    st.execute("USE `emp_db`");
                }
                st.execute(sql);
                System.out.println("[OK] " + sql.trim().split("\\s+")[0] + " "
                        + sql.trim().split("\\s+")[1] + " ...");
            }
            // 校验：索引与约束
            try (ResultSet rs = st.executeQuery("SHOW INDEX FROM emp")) {
                System.out.println("\n=== 索引 ===");
                while (rs.next()) {
                    System.out.printf("  %s -> %s (%s)%n", rs.getString("Key_name"),
                            rs.getString("Column_name"),
                            rs.getString("Non_unique").equals("0") ? "唯一" : "普通");
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM emp")) {
                rs.next();
                System.out.println("=== 数据行数 = " + rs.getInt(1) + " ===");
            }
        }
    }
}

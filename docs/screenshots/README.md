# 实验二 关键节点截图索引

本目录保存《JAVA框架技术(一)》实验二（SSM 全注解 + MyBatis-Plus 员工管理 MIS）在关键实验结点上的真实运行截图，
全部由 `docs/scripts/snap.ps1` 调用 `docs/scripts/shots/shot.bat <结点号>` 在真机（Windows + JDK 21 + Tomcat 11.0.18 + MySQL 8）上抓取，
非手工摆拍，可直接作为实验报告"三、实验内容与过程（关键步骤 + 核心代码 + 运行结果截图）"的配图。

> 安全说明：截图只包含数据库连接 URL、类名、接口响应与日志，**不含任何账号密码**；
> 真实配置 `src/main/resources/jdbc.properties` 已被 `.gitignore` 排除，仓库内只保留 `jdbc.properties.example` 模板。

| 截图 | 对应实验步骤 | 画面要点 |
| --- | --- | --- |
| [01_项目结构与全注解包划分.png](01_项目结构与全注解包划分.png) | 任务 1 / 任务 2（工程与包结构） | `com.lab.emp` 下 config / controller / service(.impl) / mapper / entity / dto / common / interceptor / aspect 九个包的类清单，体现"全注解、零 XML"分层 |
| [02_Maven打包war成功.png](02_Maven打包war成功.png) | 任务 1.4（Maven 构建） | `mvn -o clean package` 输出 `BUILD SUCCESS`，产物 `target/emp-mis.war` |
| [03_Tomcat11启动部署与未登录401.png](03_Tomcat11启动部署与未登录401.png) | 任务 3.3 / 任务 4（部署与拦截器） | Tomcat 11.0.18 + JVM 21.0.3、webapps 下 `emp-mis.war`、应用日志中根容器与 DispatcherServlet 启动、未登录访问 `/emps/1` 返回 401 JSON |
| [04_建库建表与idx_emp_name索引.png](04_建库建表与idx_emp_name索引.png) | 任务 2.3 / 任务 7.2（初始化脚本与慢查询优化） | `sql/init.sql` 建库建表 + 幂等补建 `idx_emp_name` 索引（第二次执行自动跳过） |
| [05_SSM双容器整合装配验证.png](05_SSM双容器整合装配验证.png) | 任务 3.4（SSM 全注解整合） | 根容器装配 DruidDataSource / SqlSessionFactory / 分页插件 / MapperScanner / DataSourceTransactionManager，Controller 被 excludeFilters 排除；MVC 子容器持有 EmpController 并成功回调父容器 Service |
| [06_Service与Controller分层验证.png](06_Service与Controller分层验证.png) | 任务 5.1 / 5.2（Service 与 Controller） | `pageQuery` 无条件分页与 `EmpQueryDTO` 条件查询、`listByDept`；子容器 Controller → 父容器 Service 全链路增删改查与 AOP 日志同步输出 |
| [07_RESTful查询_登录分页模糊条件.png](07_RESTful查询_登录分页模糊条件.png) | 任务 4 / 5.1 / 7.1（接口测试） | 未登录 401 → `POST /login` 成功 → 分页、按姓名/性别/部门模糊查询命中 |
| [08_RESTful查询_分页边界与单条.png](08_RESTful查询_分页边界与单条.png) | 任务 7.1（分页边界） | `page=0`、超尾页 `page=99`、`size=0` 三种边界，按 id 查询 200 与不存在 id 返回 404 |
| [09_RESTful写入_POST_PUT_DELETE.png](09_RESTful写入_POST_PUT_DELETE.png) | 任务 5.2（RESTful 语义） | POST 新增回填自增 id、PUT 全量修改薪资、DELETE 批量删除（含不存在 id 仍幂等成功）、删除后复查 404 |
| [10_参数校验与全局异常处理.png](10_参数校验与全局异常处理.png) | 任务 5.3 / 5.4（JSR-303 + 统一异常） | 空姓名 `@NotBlank`、负薪资 `@DecimalMin`、性别 `@Pattern`、超长姓名 `@Size`、非法 JSON、部门不存在、批量删除空数组，全部由 `@RestControllerAdvice` 转成 `code=400` 统一响应 |
| [11_一对多关联与CORS跨域预检.png](11_一对多关联与CORS跨域预检.png) | 任务 6 / 任务 4（关联与跨域） | `/depts/1/emps` 部门带员工列表、`/emps/with-dept` 员工带 `deptInfo`、删有员工的部门被拦截、`OPTIONS` 预检返回 200 且带 `Access-Control-Allow-*` 头 |
| [12_AOP请求日志切面输出.png](12_AOP请求日志切面输出.png) | 任务 4.3（AOP 环绕通知） | `WebLogAspect` 在 MVC 子容器生效，`[AOP REQ]` 打印方法入参、`[AOP RESP]` 打印耗时 |
| [13_声明式事务与三种失效场景.png](13_声明式事务与三种失效场景.png) | 任务 6.5（事务失效场景） | TX-1 `@Transactional` 回滚、TX-2 无注解提交、TX-3 异常被吞仍提交、TX-4 `this` 自调用绕过代理、TX-5 private 方法注解被忽略，逐条给出 `SELECT count(*)` 证据 |
| [14_数据库最终数据.png](14_数据库最终数据.png) | 任务 6 / 7.1（数据一致性复核） | 验证结束后 `ssm_emp` 恢复为 4 名员工 / 3 个部门，部门同步、级联保护、唯一约束与清理动作全部符合预期 |
| [15_Git提交历史与v1.0.0标签.png](15_Git提交历史与v1.0.0标签.png) | 任务 7.3（Git 收尾） | `feat` / `fix` / `refactor` / `chore` 规范提交轨迹与附注标签 `v1.0.0` |

## 重新抓取

单结点抓取（保留窗口 6 秒后自动关闭并截图）：

```powershell
# 在项目根目录 E:\mybatis-demo\emp-mis 下执行
powershell -NoProfile -ExecutionPolicy Bypass -File docs\scripts\snap.ps1 -Node 13 -Wait 30
```

`-Node` 取 1~15，图片按 `shot.bat` 中该结点的标题命名并覆盖同名文件；结点清单见 `docs/scripts/shots/shot.bat`。

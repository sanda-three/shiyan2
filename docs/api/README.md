# 任务 7.1 接口测试请求体样例（UTF-8）

这些文件是 `docs/scripts/api-test.sh` 与各 `docs/scripts/shots/shot*.bat` 截图脚本共用的
请求体/查询参数数据，覆盖"正常功能 + 参数校验 + 异常场景 + 分页边界"四类用例。

> 为什么用文件而不是命令行参数传中文：Windows 下 curl 收到的命令行参数按系统 ANSI
> 码页（GBK）传入，直接写中文会让服务端拿到非法 UTF-8 字节。用
> `--data-binary "@docs/api/xxx.json"` 与 `--data-urlencode "name@docs/api/query_name.txt"`
> 传文件，字节流原样为 UTF-8，与 ApiFox 提交 JSON body 的行为一致。

| 文件 | 用途 | 预期结果 |
| --- | --- | --- |
| add.json | 正常新增员工 | 200，回填 empId |
| update.json 模板由脚本按新增 id 动态生成 | 正常修改 | 200 |
| empty_name.json | @NotBlank 姓名空 | 400 参数校验失败 |
| neg_salary.json | @DecimalMin 负薪资 | 400 参数校验失败 |
| bad_gender.json | @Pattern 性别非男/女 | 400 参数校验失败 |
| long_name.json | @Size 姓名超 50 | 400 参数校验失败 |
| bad_dept.json | 部门不存在（业务校验） | 400 业务异常 |
| query_name.txt / query_gender.txt / query_dept.txt | 多条件模糊查询参数 | 命中 1 条 |
| query_new_name.txt | 新增后按姓名回查取 id | 命中新增记录 |

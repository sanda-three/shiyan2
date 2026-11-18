#!/bin/bash
# ============================================================================
# 实验二 任务 7.1：RESTful 接口系统化测试（HTTP 层）
# 前置：Tomcat 11 已启动并部署 emp-mis.war（bash docs/scripts/shots/shot03 或手动启动）
# 用法：bash docs/scripts/api-test.sh
# 请求体与中文查询参数一律取自 docs/api/（UTF-8 文件，见 docs/api/README.md），
# 避免 Git Bash 向 Windows 原生 curl 传中文参数时按 GBK 码页转换，
# 导致服务端收到非法 UTF-8 字节而报 400/500。
# ============================================================================
BASE=http://localhost:8080/emp-mis
A=docs/api
T=target/http
JAR=$T/cookie.txt
mkdir -p $T && rm -f $JAR

say() { echo; echo "[$1] $2"; }
post() { curl -s -b $JAR -X POST "$BASE$1" -H 'Content-Type: application/json' --data-binary "@$2"; }

echo "=============== 实验二 任务 7.1 接口测试矩阵（$BASE）==============="

say "T1 未登录访问业务接口(预期401)" "$(curl -s "$BASE/emps?page=1&size=2")"
say "T2 错误密码登录(预期401)" "$(curl -s -X POST "$BASE/login" -d 'username=admin&password=wrong')"
say "T3 正确登录 admin/admin123" "$(curl -s -c $JAR -X POST "$BASE/login" -d 'username=admin&password=admin123')"

say "T4 分页查询 page=1 size=2" "$(curl -s -b $JAR "$BASE/emps?page=1&size=2")"
say "T5 模糊查询 name=张" "$(curl -s -b $JAR --get --data-urlencode "name@$A/query_name.txt" "$BASE/emps")"
say "T6 组合条件 name=张 gender=男 dept=技术部" "$(curl -s -b $JAR --get \
    --data-urlencode "name@$A/query_name.txt" --data-urlencode "gender@$A/query_gender.txt" \
    --data-urlencode "dept@$A/query_dept.txt" "$BASE/emps")"
say "T7 分页边界 page=0" "$(curl -s -b $JAR "$BASE/emps?page=0&size=2" | grep -o '"total":[0-9]*\|"current":[0-9]*\|"size":[0-9]*' | tr '\n' ' ')"
say "T8 分页边界 超尾页 page=99" "$(curl -s -b $JAR "$BASE/emps?page=99&size=5")"
say "T9 分页边界 size=0" "$(curl -s -b $JAR "$BASE/emps?page=1&size=0" | grep -o '"total":[0-9]*\|"current":[0-9]*\|"size":[0-9]*' | tr '\n' ' ')"

say "T10 按 id 查询" "$(curl -s -b $JAR "$BASE/emps/1")"
say "T11 查询不存在 id(预期404)" "$(curl -s -b $JAR "$BASE/emps/99999")"

say "T12 POST 新增员工" "$(post /emps $A/add.json)"
NEW_ID=$(curl -s -b $JAR --get --data-urlencode "name@$A/query_new_name.txt" "$BASE/emps" | grep -o '"empId":[0-9]*' | head -1 | cut -d: -f2)
echo "     [NEW-ID] $NEW_ID"
sed "s/^{/{\"empId\":$NEW_ID,/" $A/update.json > $T/update.json
say "T13 PUT 修改员工（薪资 8888.88）" "$(curl -s -b $JAR -X PUT "$BASE/emps" -H 'Content-Type: application/json' --data-binary "@$T/update.json")"

say "T14 校验：空姓名(@NotBlank)" "$(post /emps $A/empty_name.json)"
say "T15 校验：负薪资(@DecimalMin)" "$(post /emps $A/neg_salary.json)"
say "T16 校验：性别非男/女(@Pattern)" "$(post /emps $A/bad_gender.json)"
say "T17 校验：姓名 52 字(@Size max=50)" "$(post /emps $A/long_name.json)"
say "T18 异常：JSON 格式错误" "$(curl -s -b $JAR -X POST "$BASE/emps" -H 'Content-Type: application/json' -d '{bad json}')"
say "T19 校验：部门不存在（业务规则）" "$(post /emps $A/bad_dept.json)"

say "T20 DELETE 批量删除（含不存在 id）" "$(curl -s -b $JAR -X DELETE "$BASE/emps" -H 'Content-Type: application/json' -d "[$NEW_ID,99999]")"
say "T21 异常：批量删除空数组" "$(curl -s -b $JAR -X DELETE "$BASE/emps" -H 'Content-Type: application/json' -d '[]')"
say "T22 删除后再查该 id(预期404)" "$(curl -s -b $JAR "$BASE/emps/$NEW_ID")"

say "T23 一对多：GET /depts/1/emps" "$(curl -s -b $JAR "$BASE/depts/1/emps")"
say "T24 一对多：GET /emps/with-dept" "$(curl -s -b $JAR "$BASE/emps/with-dept" | head -c 260)"
say "T25 级联保护：删除有员工的部门" "$(curl -s -b $JAR -X DELETE "$BASE/depts/1")"

say "T26 CORS 预检响应头(预期200)" "$(curl -s -D- -o /dev/null -X OPTIONS "$BASE/emps" \
    -H 'Origin: http://127.0.0.1:5500' -H 'Access-Control-Request-Method: GET' | grep -ia '^HTTP\|^access-control' | tr '\n' ' ')"

say "T27 POST /logout" "$(curl -s -b $JAR -c $JAR -X POST "$BASE/logout")"
say "T28 退出后访问(预期401)" "$(curl -s -b $JAR "$BASE/emps?page=1")"

curl -s -c $JAR -X POST "$BASE/login" -d 'username=admin&password=admin123' > /dev/null
say "T29 复位核对：emp 总数(预期4)" "$(curl -s -b $JAR "$BASE/emps?page=1&size=1" | grep -o '"total":[0-9]*')"
say "T30 复位核对：dept 总数(预期3)" "$(curl -s -b $JAR "$BASE/depts" | grep -o '"deptNo"' | wc -l)"

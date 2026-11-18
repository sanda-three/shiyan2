@echo off
rem ==========================================================================
rem Lab-2 key-node console output driver (used by docs/scripts/snap.ps1).
rem Usage: shot.bat 01 .. 15
rem
rem Rules learned the hard way:
rem 1) This file must stay ASCII-only. cmd reads .bat bytes with the system
rem    ANSI codepage (GBK), so Chinese inside a batch file turns into garbage
rem    "commands". All Chinese on screen comes from java/curl UTF-8 output,
rem    which renders correctly because of "chcp 65001" in _env.bat.
rem 2) Chinese request bodies / query params are passed through files
rem    (docs/api/*) with --data-binary "@file" and --data-urlencode "k@file",
rem    otherwise the server receives GBK bytes and answers 400/500.
rem 3) Output filtering is delegated to grep inside Git Bash so that the
rem    screenshot content matches docs/scripts/api-test.sh exactly.
rem 4) One node = one screenful (a maximized console shows ~45 lines), so the
rem    API matrix is split across nodes instead of dumping all 30 cases.
rem ==========================================================================
set N=%1
call "%~dp0_env.bat"
set BASH=E:\GIT\Git\bin\bash.exe
rem cmd strips double quotes inside a double-quoted argument, so the JDK path
rem (contains a space) is quoted with SINGLE quotes for bash to consume.
set JAVAQ='C:/Program Files/BellSoft/LibericaJDK-21/bin/java.exe'
if "%N%"=="01" goto s01
if "%N%"=="02" goto s02
if "%N%"=="03" goto s03
if "%N%"=="04" goto s04
if "%N%"=="05" goto s05
if "%N%"=="06" goto s06
if "%N%"=="07" goto s07
if "%N%"=="08" goto s08
if "%N%"=="09" goto s09
if "%N%"=="10" goto s10
if "%N%"=="11" goto s11
if "%N%"=="12" goto s12
if "%N%"=="13" goto s13
if "%N%"=="14" goto s14
if "%N%"=="15" goto s15
echo unknown node %N%
goto end

:s01
echo ========== [01] SSM all-annotation layout: com.lab.emp packages ==========
echo.
tree src\main\java
echo.
echo ---- classes per package ----
echo   config      : WebAppInitializer, SpringConfig, WebMvcConfig
echo   controller  : EmpController, DeptController, LoginController
echo   service     : EmpService, DeptService, TxVerifyService
echo   service.impl: EmpServiceImpl, DeptServiceImpl, TxVerifyServiceImpl
echo   mapper      : EmpMapper, DeptMapper
echo   entity      : Emp, Dept
echo   dto         : EmpQueryDTO
echo   common      : Result, BizException, GlobalExceptionHandler
echo   interceptor : LoginInterceptor, RequestLogInterceptor
echo   aspect      : WebLogAspect
echo.
echo ---- resources / sql / docs ----
dir /b src\main\resources
dir /b sql
dir /b docs\scripts
dir /b docs\api
goto end

:s02
echo ========== [02] mvn -o clean package : build war ==========
echo.
call "%MVN%" -o clean package -DskipTests
echo.
dir target\emp-mis.war
goto end

:s03
echo ========== [03] Tomcat 11 running + emp-mis.war deployed ==========
echo.
call "E:\TOMCAT\apache-tomcat-11.0.18\bin\version.bat" | findstr /i /c:"Server version" /c:"JVM"
echo.
echo ---- webapps (emp-mis war + exploded dir) ----
dir /b E:\TOMCAT\apache-tomcat-11.0.18\webapps | findstr /i emp
echo.
echo ---- application log: Spring root ctx + DispatcherServlet init ----
powershell -NoProfile -Command "Get-Content 'E:\TOMCAT\apache-tomcat-11.0.18\logs\emp-mis.log' -TotalCount 6 -Encoding UTF8"
echo.
echo ---- GET /emp-mis/emps/1 without session (LoginInterceptor 401) ----
curl -s -i "%BASE%/emps/1"
goto end

:s04
echo ========== [04] step 4.1 init.sql : DDL, constraints, idx_emp_name ==========
echo.
call "%~dp0runtool.bat" Step41SqlRunner
goto end

:s05
echo ========== [05] step 3.4 SSM container integration (root + MVC child) ==========
echo.
call "%~dp0runtool.bat" Step34VerifyTool
goto end

:s06
echo ========== [06] step 5.1/5.2 service + controller (EmpQueryDTO paging) ==========
echo.
call "%~dp0runtool.bat" Step42VerifyTool
goto end

:s07
echo ========== [07] RESTful read APIs : 401 / login / paging / fuzzy query ==========
echo.
%BASH% -c "cd /e/mybatis-demo/emp-mis && bash docs/scripts/api-test.sh | grep -aE '^\[T(1|2|3|4|5|6) '"
goto end

:s08
echo ========== [08] RESTful read APIs : paging bounds, get by id, 404 ==========
echo.
%BASH% -c "cd /e/mybatis-demo/emp-mis && bash docs/scripts/api-test.sh | grep -aE '^\[T(7|8|9|10|11) '"
goto end

:s09
echo ========== [09] RESTful write APIs : POST / PUT / DELETE batch ==========
echo.
%BASH% -c "cd /e/mybatis-demo/emp-mis && bash docs/scripts/api-test.sh | grep -aE '^\[T(12|13|20|22) |NEW-ID'"
goto end

:s10
echo ========== [10] JSR-303 validation + GlobalExceptionHandler ==========
echo.
%BASH% -c "cd /e/mybatis-demo/emp-mis && bash docs/scripts/api-test.sh | grep -aE '^\[T(14|15|16|17|18|19|21) '"
goto end

:s11
echo ========== [11] one-to-many dept/emp + CORS preflight ==========
echo.
%BASH% -c "cd /e/mybatis-demo/emp-mis && bash docs/scripts/api-test.sh | grep -aE '^\[T(23|24|25|26) '"
goto end

:s12
echo ========== [12] AOP WebLogAspect output in Tomcat application log ==========
echo.
powershell -NoProfile -Command "Select-String -Path 'E:\TOMCAT\apache-tomcat-11.0.18\logs\emp-mis.log' -Pattern 'AOP' | Select-Object -Last 16 | ForEach-Object { $_.Line }"
goto end

:s13
echo ========== [13] step 6.5 transaction: rollback + 3 failure scenarios ==========
echo.
call "%~dp0runtool.bat" StepTxVerifyTool
goto end

:s14
echo ========== [14] final rows in MySQL ssm_emp (emp / dept) ==========
echo.
call "%~dp0runtool.bat" Step6VerifyTool
goto end

:s15
echo ========== [15] git history + annotated tag v1.0.0 ==========
echo.
git log --oneline --graph -10
echo.
git tag -n
echo.
git show --stat --oneline -s HEAD
goto end

:end

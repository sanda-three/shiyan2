@echo off
rem Shared env for the screenshot scripts: codepage, tool paths, and lazy
rem preparation of test-classes / dependency classpath file.
chcp 65001 >nul
cd /d E:\mybatis-demo\emp-mis
set "JAVA=C:\Program Files\BellSoft\LibericaJDK-21\bin\java.exe"
set "MVN=E:\apache-maven-3.9.6-bin\apache-maven-3.9.6\bin\mvn.cmd"
set BASE=http://localhost:8080/emp-mis
set JAR=target\http\cookie.txt
if not exist target\cp.txt call "%MVN%" -o -q dependency:build-classpath -Dmdep.outputFile=target/cp.txt -Dmdep.includeScope=test
if not exist target\test-classes call "%MVN%" -o -q test-compile
rem NOTE: "set /p" truncates input at 1023 chars, which silently drops jars from
rem the classpath. Read the whole line with for /f instead (cp.txt is ~4.4 KB).
set CP=
for /f "usebackq delims=" %%i in ("target\cp.txt") do set "CP=%%i"
set RUNCP=target\classes;target\test-classes;%CP%

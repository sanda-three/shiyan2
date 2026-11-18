@echo off
rem Start Tomcat 11 in a visible console window (emp-mis.war must be in webapps)
chcp 65001 >nul
set "JAVA_HOME=C:\Program Files\BellSoft\LibericaJDK-21"
cd /d "E:\TOMCAT\apache-tomcat-11.0.18\bin"
call startup.bat

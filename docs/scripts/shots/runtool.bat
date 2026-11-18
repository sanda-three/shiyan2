@echo off
rem ==========================================================================
rem Run one verification tool with clean console output (no log4j noise),
rem so a screenshot node shows only the tool's own evidence lines.
rem Usage: runtool.bat StepTxVerifyTool
rem Kept as pure cmd: nested quotes inside bash -c are unreliable, and cmd can
rem quote the JDK path (which contains a space) natively.
rem ==========================================================================
call "%~dp0_env.bat"
"%JAVA%" -Dlog4j.configuration=log4j-shot.properties -Dstdout.encoding=UTF-8 -cp "docs;%RUNCP%" com.lab.emp.%1

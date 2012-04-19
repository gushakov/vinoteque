echo off
set JAVA_HOME=C:\jdk1.6.0_29
set HSQLDB_HOME=C:\prog\hsqldb
echo ---------------------------
echo Starting HSQLDB manager GUI
echo ---------------------------
cd %HSQLDB_HOME%\lib
%JAVA_HOME%\bin\java.exe -cp hsqldb.jar org.hsqldb.util.DatabaseManagerSwing

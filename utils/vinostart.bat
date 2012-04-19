echo off
set JAVA_HOME=C:\jdk1.6.0_29
set HSQLDB_HOME=C:\prog\hsqldb
set VINOTEQUE_HOME=C:\vinoteque
echo --------------------------------------
echo Starting HSQLDB database for vinoteque
echo --------------------------------------
cd %VINOTEQUE_HOME%\hsqldb
%JAVA_HOME%\bin\java.exe -cp %HSQLDB_HOME%\lib\hsqldb.jar org.hsqldb.Server

@ECHO OFF

rem Saving the classpath so we can restore it at the end.
SET OLDCLASSPATH=%CLASSPATH%

rem Assume this is invoked via in MASON_HOME

rem set STARTING_POINT=%CD%

set MASON_HOME=%CD%

rem ...feel free to set MASON_DIR to the right thing.


rem You need MASON_HOME in classpath to run _java ... sim.*


call "%MASON_HOME%"\start\ignoreme.bat %MASON_HOME%

rem echo %MASON_HOME%

SET JARS=%MASON_HOME%\lib

SET CLASSPATH=%MASON_HOME%;%JARS%\jfreechart-1.0.1.jar;%JARS%\jcommon-1.0.0.jar;%JARS%\itext-1.4.5.jar;%JARS%\quaqua-colorchooser-only.jar

rem echo %JARS%

rem adding all jars in the jar directory to the classpath.
rem (for /F %%f IN ('dir /b /a-d %JARS%\*.jar') do call %MASON_HOME%\start\ignoreme.bat "%JARS%\%%f%")
rem 2>nul

java -Xmx256M sim.app.sugarscape.SugarscapeWithUIHigh -file conf/fig2-2.conf

rem Restoring the classpath.
SET CLASSPATH=%OLDCLASSPATH%

rem code below was taken from fig2-2.sh
rem libjar=lib/jfreechart-1.0.1.jar:lib/jcommon-1.0.0.jar:lib/quaqua-colorchooser-only.jar:lib/itext-1.4.5.jar

rem java -classpath .:$libjar sim.app.sugarscape.SugarscapeWithUIHigh -file conf/fig2-2.conf

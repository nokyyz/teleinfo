@echo off

:: set path to eclipse folder. If local folder, use '.'; otherwise, use c:\path\to\eclipse
set ECLIPSEHOME="runtime/server"

:: get path to equinox jar inside ECLIPSEHOME folder
:check_path
for /f "delims= tokens=1" %%c in ('dir /B /S /OD %ECLIPSEHOME%\plugins\org.eclipse.equinox.launcher_*.jar') do set EQUINOXJAR=%%c

IF NOT [%EQUINOXJAR%] == [] GOTO :Launch
echo No equinox launcher in path '%ECLIPSEHOME%' found!
goto :eof

:Launch 
:: start Eclipse w/ java
echo Launching Teleinfo runtime...
java ^
-Dopenhab.logdir=./userdata/logs ^
-Dlogback.configurationFile=./runtime/etc/logback.xml ^
-Djava.library.path=./lib ^
-jar %EQUINOXJAR% %* ^
-console 

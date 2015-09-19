goto :start
:start
%echo off
cd Deployment/build

if not exist ../apache-ant-1.9.6 goto :error

cd ../
build/fciv -v -xml build/out.bin -md5 apache-ant-1.9.6
cd build

if not %ERRORLEVEL%==0 goto :error

echo.

call ../apache-ant-1.9.6/bin/ant.bat -f build.xml

echo.

java -jar jarsplice+.jar -i temp/Base.jar temp/Base_lib/*.jar -n temp/Base_lib/natives/* -m astechzgo.luminescent.main.Main -o ../Luminescent.jar

echo.

echo The output jar has been placed in the Deployment folder
pause

goto :eof

:error
echo.
echo ANT is either corrupt or does not exist.  Downloading now...
call updateANT.bat
pause

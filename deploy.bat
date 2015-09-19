%echo off
cd Deployment

echo.

call apache-ant-1.9.5/bin/ant.bat -f build.xml

echo.

java -jar jarsplice+.jar -i temp/Base.jar temp/Base_lib/*.jar -n temp/Base_lib/natives/* -m astechzgo.luminescent.main.Main -o Luminescent.jar

echo.

echo The output jar has been placed in the Deployment folder
pause
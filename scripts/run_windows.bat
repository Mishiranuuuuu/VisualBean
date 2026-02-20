@echo off
:: Move to project root
pushd "%~dp0\.."

if not exist bin mkdir bin
javac -d bin -sourcepath src src/com/visualbean/Main.java
java -cp bin com.visualbean.Main

popd
pause

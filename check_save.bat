@echo off
if not exist bin mkdir bin
javac -d bin -sourcepath src src\com\vnengine\tools\SaveInspector.java
java -cp bin com.vnengine.tools.SaveInspector
pause
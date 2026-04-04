@ECHO OFF
SET DIR=%~dp0
IF NOT EXIST "%DIR%gradle\wrapper\gradle-wrapper.jar" (
  ECHO gradle-wrapper.jar nao encontrado. Gere o wrapper com "gradle wrapper" ou baixe o projeto base pelo Spring Initializr.
  EXIT /B 1
)
"%JAVA_HOME%\bin\java.exe" -classpath "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

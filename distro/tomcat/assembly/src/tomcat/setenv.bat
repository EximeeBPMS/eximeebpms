set CATALINA_OPTS=-Xmx512m

REM silence JDK restricted native access warning raised by GraalJS (Truffle); available since JDK 17
set CATALINA_OPTS=%CATALINA_OPTS% --enable-native-access=ALL-UNNAMED

REM silence JDK sun.misc.Unsafe deprecation warning raised by GraalJS (Truffle); flag only exists since JDK 23
set JAVA_BIN=java
IF NOT "x%JAVA_HOME%" == "x" (
  set JAVA_BIN=%JAVA_HOME%\bin\java
)
FOR /f "tokens=3" %%g IN ('"%JAVA_BIN%" -version 2^>^&1 ^| findstr /i "version"') DO (
  set JAVA_VERSION=%%g
)
set JAVA_VERSION=%JAVA_VERSION:"=%
FOR /f "delims=. tokens=1" %%v in ("%JAVA_VERSION%") do (
  IF %%v GEQ 23 (
    set CATALINA_OPTS=%CATALINA_OPTS% --sun-misc-unsafe-memory-access=allow
  )
)

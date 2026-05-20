@echo off
setlocal enabledelayedexpansion

set "BIN_DIR=%~dp0"
set "JAR_FILE=%BIN_DIR%target\backend-0.0.1-SNAPSHOT.jar"
set "ENV_FILE=%BIN_DIR%.env"
set "LOG_DIR=%BIN_DIR%logs"

if not exist "%JAR_FILE%" (
    echo JAR not found: %JAR_FILE%
    echo Run 'mvn clean package -DskipTests' first.
    exit /b 1
)

if not exist "%ENV_FILE%" (
    echo .env not found: %ENV_FILE%
    exit /b 1
)

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

for /f "usebackq delims=" %%i in ("%ENV_FILE%") do (
    set "line=%%i"
    if not "!line!"=="" if "!line:~0,1!" neq "#" (
        for /f "tokens=1,* delims==" %%a in ("!line!") do (
            set "key=%%a"
            set "val=%%b"
            if defined val (
                set "!key!=!val!"
            )
        )
    )
)

"%JAVA_HOME%\bin\java" ^
    -Xmx512m -Xms256m ^
    -jar "%JAR_FILE%" ^
    --server.port=8080 ^
    >> "%LOG_DIR%\startup.log" 2>&1

endlocal

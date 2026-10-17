@echo off
cd /d "%~dp0"

:: Set Maven in PATH if available
if exist "C:\Users\JEYRAM\apache-maven-3.9.6\bin" (
    set "PATH=C:\Users\JEYRAM\apache-maven-3.9.6\bin;%PATH%"
)

:: If target jar does not exist, build it
if not exist "target\mystery-vault-1.0.0.jar" (
    echo Building and packaging Mystery Vault...
    call powershell -ExecutionPolicy Bypass -File .\build.ps1
)

echo Launching Mystery Vault - Uncover the Truth...
java -jar "target\mystery-vault-1.0.0.jar"

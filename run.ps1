# Universal launcher for Mystery Vault

# Stop any previous instances to release Windows file locks
Get-CimInstance Win32_Process -ErrorAction SilentlyContinue | Where-Object { 
    $_.CommandLine -like "*mystery-vault*" -and $_.ProcessId -ne $PID 
} | ForEach-Object {
    Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
}

# Ensure Maven is on PATH
if (!(Get-Command mvn -ErrorAction SilentlyContinue) -and (Test-Path "C:\Users\JEYRAM\apache-maven-3.9.6\bin\mvn.cmd")) {
    $env:PATH = "C:\Users\JEYRAM\apache-maven-3.9.6\bin;" + $env:PATH
}

# Auto-build if JAR does not exist
if (!(Test-Path "target\mystery-vault-1.0.0.jar")) {
    Write-Host "Building and packaging Mystery Vault..."
    & powershell -ExecutionPolicy Bypass -File .\build.ps1
}

Write-Host "Launching Mystery Vault - Uncover the Truth..."
java -jar "target\mystery-vault-1.0.0.jar"

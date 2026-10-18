# Comprehensive build, test, and demo script for Member 3
$ErrorActionPreference = "Stop"

$workspaceRoot = $PSScriptRoot
$classesDir = Join-Path $workspaceRoot "target\classes"
$testClassesDir = Join-Path $workspaceRoot "target\test-classes"

New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
New-Item -ItemType Directory -Force -Path $testClassesDir | Out-Null

Write-Host "=========================================================="
Write-Host " [1/4] Compiling src/main/java (Member 1 & Member 3 Modules)..."
Write-Host "=========================================================="
$mainFiles = Get-ChildItem -Path (Join-Path $workspaceRoot "src\main\java") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -d $classesDir $mainFiles
if ($LASTEXITCODE -ne 0) { throw "Compilation of main sources failed" }
Write-Host "Main sources compiled successfully."

Write-Host "`n=========================================================="
Write-Host " [2/4] Compiling src/test/java..."
Write-Host "=========================================================="
$m2Repo = Join-Path $env:USERPROFILE ".m2\repository"
$junitApi = Join-Path $m2Repo "org\junit\jupiter\junit-jupiter-api\5.10.2\junit-jupiter-api-5.10.2.jar"
$junitCommons = Join-Path $m2Repo "org\junit\platform\junit-platform-commons\1.10.2\junit-platform-commons-1.10.2.jar"
$opentest = Join-Path $m2Repo "org\opentest4j\opentest4j\1.3.0\opentest4j-1.3.0.jar"
$apiguardian = Join-Path $m2Repo "org\apiguardian\apiguardian-api\1.1.2\apiguardian-api-1.1.2.jar"

$testClasspath = "$classesDir;$junitApi;$junitCommons;$opentest;$apiguardian"
$testFiles = Get-ChildItem -Path (Join-Path $workspaceRoot "src\test\java") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -cp $testClasspath -d $testClassesDir $testFiles
if ($LASTEXITCODE -ne 0) { throw "Compilation of test sources failed" }
Write-Host "Test sources compiled successfully."

Write-Host "`n=========================================================="
Write-Host " [3/4] Running Automated Tests (Member 1 + Member 3)..."
Write-Host "=========================================================="
$runtimeClasspath = "$classesDir;$testClassesDir;$junitApi;$junitCommons;$opentest;$apiguardian"
& java -cp $runtimeClasspath com.mysteryvault.Member3TestSuite
if ($LASTEXITCODE -ne 0) { throw "Automated test suite failed" }

Write-Host "`n=========================================================="
Write-Host " [4/4] Running Member 3 End-to-End Verification Demo..."
Write-Host "=========================================================="
& java -cp $classesDir com.mysteryvault.Member3VerificationDemo
if ($LASTEXITCODE -ne 0) { throw "Verification demo failed" }

Write-Host "`nAll build, test, and verification steps succeeded!"

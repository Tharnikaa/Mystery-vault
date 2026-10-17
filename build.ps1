$sources = Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
if (!(Test-Path "target/classes")) {
    New-Item -ItemType Directory -Force -Path "target/classes" | Out-Null
}
Write-Host "Compiling $($sources.Count) Java main source files..."
javac -encoding UTF-8 -d target/classes $sources
if ($LASTEXITCODE -ne 0) {
    Write-Host "Main compilation failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

Write-Host "Compilation successful!"
Copy-Item -Recurse -Force "src/main/resources/*" "target/classes/" -ErrorAction SilentlyContinue
Write-Host "Resources copied."

# Compile tests if present
$testSources = Get-ChildItem -Path "src/test/java" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
if ($testSources -and $testSources.Count -gt 0) {
    if (!(Test-Path "target/test-classes")) {
        New-Item -ItemType Directory -Force -Path "target/test-classes" | Out-Null
    }
    Write-Host "Compiling $($testSources.Count) test files..."
    javac -encoding UTF-8 -cp "target/classes" -d target/test-classes $testSources
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Running CaseSolverEngineTest..."
        java -cp "target/classes;target/test-classes" com.mysteryvault.service.CaseSolverEngineTest
    }
}

# Package JAR
jar cfe target/mystery-vault-1.0.0.jar com.mysteryvault.MysteryVaultApplication -C target/classes .
Write-Host "Packaged target/mystery-vault-1.0.0.jar"

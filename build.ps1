# Mystery Vault Build & Test Script

# Ensure any running Mystery Vault process is stopped so file locks on target/*.jar are released
Get-CimInstance Win32_Process -ErrorAction SilentlyContinue | Where-Object { 
    $_.CommandLine -like "*mystery-vault*" -and $_.ProcessId -ne $PID 
} | ForEach-Object {
    Write-Host "Closing running instance of Mystery Vault (PID: $($_.ProcessId)) to release file locks..."
    Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
}

$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if (!$mvnCmd) {
    $candidates = @(
        "$env:USERPROFILE\.m2\apache-maven-3.9.6\bin",
        "C:\Users\varsh\.m2\apache-maven-3.9.6\bin",
        "C:\Users\JEYRAM\apache-maven-3.9.6\bin"
    )
    foreach ($cand in $candidates) {
        if (Test-Path "$cand\mvn.cmd") {
            $env:PATH = "$cand;" + $env:PATH
            $mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
            break
        }
    }
}

if ($mvnCmd) {
    Write-Host "Building and testing with Maven..."
    mvn clean test
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Maven tests failed with exit code $LASTEXITCODE"
        exit $LASTEXITCODE
    }
    mvn package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Maven package failed with exit code $LASTEXITCODE"
        exit $LASTEXITCODE
    }
    Write-Host "Build and packaging completed successfully!"
    exit 0
}

# Fallback: Pure JDK compile & test runner
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

    $m2Jars = Get-ChildItem -Path "$env:USERPROFILE\.m2\repository" -Recurse -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
    $testCp = "target/classes"
    if ($m2Jars) {
        $testCp += ";" + ($m2Jars -join ";")
    }

    Write-Host "Compiling $($testSources.Count) test files..."
    javac -encoding UTF-8 -cp $testCp -d target/test-classes $testSources
    if ($LASTEXITCODE -eq 0) {
        $runCp = "target/classes;target/test-classes"
        if ($m2Jars) {
            $runCp += ";" + ($m2Jars -join ";")
        }
        Write-Host "Running CaseSolverEngineTest..."
        java -cp $runCp com.mysteryvault.service.CaseSolverEngineTest
        Write-Host "Running ResultPanelTest..."
        java -cp $runCp com.mysteryvault.ui.swing.screens.ResultPanelTest
        Write-Host "Running ConnectionPanelTest..."
        java -cp $runCp com.mysteryvault.ui.swing.screens.ConnectionPanelTest
    }
}

# Package JAR
jar cfe target/mystery-vault-1.0.0.jar com.mysteryvault.MysteryVaultApplication -C target/classes .
Write-Host "Packaged target/mystery-vault-1.0.0.jar"

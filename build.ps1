$sources = Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
if (!(Test-Path "target/classes")) {
    New-Item -ItemType Directory -Force -Path "target/classes" | Out-Null
}
Write-Host "Compiling $($sources.Count) Java source files..."
javac -encoding UTF-8 -d target/classes $sources
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!"
    Copy-Item -Recurse -Force "src/main/resources/*" "target/classes/" -ErrorAction SilentlyContinue
    Write-Host "Resources copied."
} else {
    Write-Host "Compilation failed with exit code $LASTEXITCODE"
}

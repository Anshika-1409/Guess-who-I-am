$ErrorActionPreference = "Stop"

$buildDir = Join-Path $PSScriptRoot "build"
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null

$sources = Get-ChildItem -Path (Join-Path $PSScriptRoot "src") -Recurse -Filter "*.java" |
    ForEach-Object { $_.FullName }

javac -d $buildDir $sources
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java -cp $buildDir guesswho.GameEngineTest
exit $LASTEXITCODE

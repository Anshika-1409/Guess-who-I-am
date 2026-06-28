$ErrorActionPreference = "Stop"

$healthUrl = "http://127.0.0.1:4173/api/health"

try {
    $health = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 1
    if ($health.status -eq "ok") {
        Write-Host "Guess Who I Am is already running at http://127.0.0.1:4173/"
        Write-Host "Engine: $($health.engine), known targets: $($health.knowledgeSize)"
        exit 0
    }
} catch {
    # No compatible server is running, so continue and start one.
}

$buildDir = Join-Path $PSScriptRoot "build"
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null

$sources = Get-ChildItem -Path (Join-Path $PSScriptRoot "src") -Recurse -Filter "*.java" |
    ForEach-Object { $_.FullName }

javac -d $buildDir $sources
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

java -cp $buildDir guesswho.GameServer
exit $LASTEXITCODE

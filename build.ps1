# Windows PowerShell 5.1 / PowerShell 7
# 실행 위치와 무관하게 이 스크립트가 있는 저장소를 기준으로 빌드합니다.
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$repoRoot = $PSScriptRoot
$uiDirectory = Join-Path $repoRoot 'ticket-ui'
$apiDirectory = Join-Path $repoRoot 'ticket-api'
$distDirectory = Join-Path $uiDirectory 'dist'
$staticDirectory = Join-Path $apiDirectory 'src\main\resources\static'

# npm.cmd/mvnw.cmd는 PowerShell 예외 대신 종료 코드로 실패를 알릴 수 있습니다.
# 0이 아닌 종료 코드라면 즉시 중단해 이전 빌드 결과를 복사하지 않습니다.
function Invoke-Checked {
    param(
        [string] $Command,
        [string[]] $CommandArguments
    )

    & $Command @CommandArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed (exit $LASTEXITCODE): $Command $($CommandArguments -join ' ')"
    }
}

try {
    Push-Location $repoRoot
    try {
        Write-Host '[1/6] TypeScript tests'
        Invoke-Checked 'npm.cmd' @('test')
    } finally {
        Pop-Location
    }

    Push-Location $uiDirectory
    try {
        Write-Host '[2/6] Frontend tests'
        Invoke-Checked 'npm.cmd' @('test')
        Write-Host '[3/6] Frontend lint'
        Invoke-Checked 'npm.cmd' @('run', 'lint')
        Write-Host '[4/6] Frontend build'
        Invoke-Checked 'npm.cmd' @('run', 'build')
    } finally {
        Pop-Location
    }

    if (-not (Test-Path -LiteralPath (Join-Path $distDirectory 'index.html') -PathType Leaf)) {
        throw 'Frontend build did not produce dist/index.html.'
    }

    Write-Host '[5/6] Replace generated static files'
    # static은 React 빌드 산출물 전용 폴더입니다.
    # 삭제 전 절대 경로가 저장소 내부인지 확인하고 링크/정션 경로는 거절합니다.
    $expectedStatic = [System.IO.Path]::GetFullPath($staticDirectory)
    $rootPrefix = [System.IO.Path]::GetFullPath($repoRoot).TrimEnd('\') + '\'
    if (-not $expectedStatic.StartsWith($rootPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw 'Static output must stay inside this repository.'
    }
    $pathToCheck = $expectedStatic
    while ($pathToCheck.StartsWith($rootPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        if (Test-Path -LiteralPath $pathToCheck) {
            $item = Get-Item -LiteralPath $pathToCheck -Force
            if (($item.Attributes -band [System.IO.FileAttributes]::ReparsePoint) -ne 0) {
                throw "Refusing to replace output through a link/junction: $pathToCheck"
            }
        }
        $pathToCheck = Split-Path -Parent $pathToCheck
    }
    if (Test-Path -LiteralPath $expectedStatic) {
        # 이전 해시 이름의 JS/CSS가 남지 않도록 출력 폴더를 교체합니다.
        Remove-Item -LiteralPath $expectedStatic -Recurse -Force
    }
    New-Item -ItemType Directory -Path $expectedStatic -Force | Out-Null
    Copy-Item -Path (Join-Path $distDirectory '*') -Destination $expectedStatic -Recurse -Force

    Push-Location $apiDirectory
    try {
        Write-Host '[6/6] Backend tests and executable JAR packaging'
        Invoke-Checked '.\mvnw.cmd' @('clean', 'package')
    } finally {
        Pop-Location
    }

    Write-Host 'Build complete. Run from ticket-api:'
    Write-Host 'java -jar .\target\ticket-api-0.0.1-SNAPSHOT.jar'
} catch {
    # 실패를 호출한 터미널/다른 자동화에도 전달합니다.
    [Console]::Error.WriteLine($_.Exception.Message)
    exit 1
}

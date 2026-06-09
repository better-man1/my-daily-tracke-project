param(
    [switch]$SkipInstall,
    [switch]$Force
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Resolve-Path (Join-Path $ScriptDir "..")
$BackendDir = Join-Path $RootDir "backend"
$VueDir = Join-Path $RootDir "frontend"
$ReactDir = Join-Path $RootDir "react-frontend"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Write-Ok {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Assert-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command not found: $Name"
    }

    Write-Ok "Found command: $Name"
}

function Test-PortListening {
    param([int]$Port)

    $connection = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue |
        Select-Object -First 1

    return $null -ne $connection
}

function ConvertTo-PowerShellLiteral {
    param([string]$Value)
    return "'" + ($Value -replace "'", "''") + "'"
}

function Install-NodeDependenciesIfNeeded {
    param(
        [string]$Name,
        [string]$Directory
    )

    $nodeModules = Join-Path $Directory "node_modules"
    if (Test-Path $nodeModules) {
        Write-Ok "$Name dependencies already installed"
        return
    }

    if ($SkipInstall) {
        Write-Warn "$Name node_modules not found, skipped npm install because -SkipInstall was set"
        return
    }

    Write-Step "Installing $Name dependencies"
    Push-Location $Directory
    try {
        npm install
        if ($LASTEXITCODE -ne 0) {
            throw "npm install failed in $Directory"
        }
    }
    finally {
        Pop-Location
    }
}

function Start-DevProcess {
    param(
        [string]$Name,
        [string]$Directory,
        [string]$Command,
        [int]$Port
    )

    if ((Test-PortListening $Port) -and (-not $Force)) {
        Write-Warn "$Name was not started because port $Port is already in use. Use -Force to start anyway."
        return
    }

    $literalDirectory = ConvertTo-PowerShellLiteral $Directory
    $windowTitle = "Daily Tracker - $Name"
    $processCommand = @"
Set-Location -LiteralPath $literalDirectory
`$Host.UI.RawUI.WindowTitle = '$windowTitle'
Write-Host 'Starting $Name in $Directory'
$Command
"@

    Start-Process powershell.exe -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy",
        "Bypass",
        "-Command",
        $processCommand
    )

    Write-Ok "Started $Name on port $Port"
}

Write-Step "Checking project directories"
foreach ($dir in @($BackendDir, $VueDir, $ReactDir)) {
    if (-not (Test-Path $dir)) {
        throw "Directory not found: $dir"
    }
    Write-Ok "Found directory: $dir"
}

Write-Step "Checking required commands"
Assert-Command "java"
Assert-Command "mvn"
Assert-Command "node"
Assert-Command "npm"

Write-Step "Checking frontend dependencies"
Install-NodeDependenciesIfNeeded "Vue frontend" $VueDir
Install-NodeDependenciesIfNeeded "React frontend" $ReactDir

Write-Step "Starting local services"
Start-DevProcess "Backend" $BackendDir "mvn spring-boot:run" 8080
Start-DevProcess "Vue frontend" $VueDir "npm run dev -- --host 127.0.0.1" 5173
Start-DevProcess "React frontend" $ReactDir "npm run dev -- --host 127.0.0.1" 5179

Write-Host ""
Write-Host "Local startup commands have been launched." -ForegroundColor Green
Write-Host "Backend API:     http://localhost:8080"
Write-Host "Knife4j docs:    http://localhost:8080/doc.html"
Write-Host "Vue frontend:    http://localhost:5173"
Write-Host "React frontend:  http://localhost:5179"
Write-Host ""
Write-Host "If a port was already in use, the matching service was skipped."
Write-Host "Use scripts\start-local.ps1 -Force only when you intentionally want to start anyway."

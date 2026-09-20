param(
    [string]$ProjectRoot = $(Split-Path -Parent $MyInvocation.MyCommand.Path)
)

$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

function Get-JavaMajorVersion {
    param([string]$JavaExe)
    try {
        $versionOutput = & $JavaExe -version 2>&1 | Out-String
        if ($versionOutput -match 'version\s+"(?<version>[0-9]+)') {
            return [int]$matches['version']
        }
    } catch {
        return 0
    }
    return 0
}

function Resolve-JavaFromPath {
    try {
        $cmd = Get-Command java -ErrorAction Stop
        $javaExe = $cmd.Source
        if (-not [string]::IsNullOrWhiteSpace($javaExe) -and (Test-Path $javaExe)) {
            $major = Get-JavaMajorVersion -JavaExe $javaExe
            if ($major -ge 17) {
                return $javaExe
            }
        }
    } catch {
        return $null
    }
    return $null
}

$runtimeRoot = Join-Path $ProjectRoot '.runtime'
$runtimeJre = Join-Path $runtimeRoot 'jre17'
$localJava = Join-Path $runtimeJre 'bin\java.exe'

if (Test-Path $localJava) {
    Write-Output $localJava
    exit 0
}

$pathJava = Resolve-JavaFromPath
if ($pathJava) {
    Write-Output $pathJava
    exit 0
}

New-Item -ItemType Directory -Path $runtimeRoot -Force | Out-Null
$downloadDir = Join-Path $runtimeRoot 'downloads'
New-Item -ItemType Directory -Path $downloadDir -Force | Out-Null

$zipPath = Join-Path $downloadDir 'jre17-windows-x64.zip'
$extractRoot = Join-Path $runtimeRoot 'extract-tmp'

if (Test-Path $extractRoot) {
    Remove-Item -Recurse -Force $extractRoot
}
New-Item -ItemType Directory -Path $extractRoot -Force | Out-Null

$downloadUrl = 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk'
Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath -UseBasicParsing

Expand-Archive -Path $zipPath -DestinationPath $extractRoot -Force

$extractedFolder = Get-ChildItem -Path $extractRoot -Directory | Select-Object -First 1
if (-not $extractedFolder) {
    throw 'Cannot detect extracted Java runtime folder.'
}

if (Test-Path $runtimeJre) {
    Remove-Item -Recurse -Force $runtimeJre
}
Move-Item -Path $extractedFolder.FullName -Destination $runtimeJre -Force

if (-not (Test-Path $localJava)) {
    throw 'Bootstrap completed but java.exe was not found.'
}

Write-Output $localJava

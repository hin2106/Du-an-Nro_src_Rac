#Requires -RunAsAdministrator
param(
    [int]$GamePort = 14445,
    [switch]$EnableHttp = $true,
    [switch]$EnableHttps = $true
)

$ErrorActionPreference = 'Stop'

function Ensure-InboundTcpRule([string]$Name, [int]$Port) {
    $existing = Get-NetFirewallRule -DisplayName $Name -ErrorAction SilentlyContinue
    if ($null -eq $existing) {
        New-NetFirewallRule -DisplayName $Name -Direction Inbound -Action Allow `
            -Protocol TCP -LocalPort $Port -Profile Any | Out-Null
    }
}

Ensure-InboundTcpRule 'NRO Game Public' $GamePort
if ($EnableHttp) { Ensure-InboundTcpRule 'NRO Web HTTP' 80 }
if ($EnableHttps) { Ensure-InboundTcpRule 'NRO Web HTTPS' 443 }

Write-Host 'Firewall rules are ready.' -ForegroundColor Green
Write-Host 'Keep MySQL 3306 and the game backend 14446 bound to 127.0.0.1 only.' -ForegroundColor Yellow
Write-Host 'Do not remove the existing RDP rule while administering the server remotely.' -ForegroundColor Yellow

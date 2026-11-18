# ===========================================================================
# Lab-2 screenshot helper (see README-comment in Chinese below, kept ASCII-only
# because Windows PowerShell 5.1 parses .ps1 files with the ANSI codepage when
# the file has no BOM, so non-ASCII comments break the parser).
#
# What it does:
#   1. starts a MAXIMIZED cmd window running docs/scripts/shots/shot.bat <Node>
#   2. waits <Wait> seconds for the node command to finish printing
#   3. captures the primary screen into docs/screenshots/<title>.png
#   4. kills the temp window (and its java/curl children) unless -NoKill
#
# Titles come from docs/scripts/snap.txt (UTF-8, "01=name" lines) because
# command-line arguments are passed in the system ANSI codepage, which would
# mangle Chinese names given straight from Git Bash.
#
# Usage:
#   powershell -NoProfile -ExecutionPolicy Bypass -File docs/scripts/snap.ps1 -Node 01 -Wait 6
# ===========================================================================
param(
    [Parameter(Mandatory = $true)][string]$Node,
    [int]$Wait = 6,
    [switch]$NoKill
)

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

$root = 'E:\mybatis-demo\emp-mis'
$shotBat = Join-Path $root 'docs\scripts\shots\shot.bat'
$namesFile = Join-Path $root 'docs\scripts\snap.txt'
$outDir = Join-Path $root 'docs\screenshots'
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir | Out-Null }

$title = $Node
if (Test-Path $namesFile) {
    foreach ($line in [System.IO.File]::ReadAllLines($namesFile, [System.Text.Encoding]::UTF8)) {
        if ($line -like ($Node + '=*')) { $title = $line.Substring($Node.Length + 1); break }
    }
}
$safe = ($title -replace '[\\/:*?"<>|]', '')
$outPng = Join-Path $outDir ("{0}.png" -f $safe)

$proc = Start-Process -FilePath $env:ComSpec `
    -ArgumentList @('/k', ('"{0}" {1}' -f $shotBat, $Node)) -WindowStyle Maximized -PassThru
Write-Host ("node {0} pid {1} wait {2}s" -f $Node, $proc.Id, $Wait)
Start-Sleep -Seconds $Wait

$bounds = [System.Windows.Forms.Screen]::PrimaryScreen.Bounds
$bmp = New-Object System.Drawing.Bitmap $bounds.Width, $bounds.Height
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.CopyFromScreen(0, 0, 0, 0, $bounds.Size)
$bmp.Save($outPng, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose()
Write-Host ("saved {0} {1}x{2}" -f $outPng, $bounds.Width, $bounds.Height)

if (-not $NoKill) {
    Start-Process -FilePath $env:ComSpec `
        -ArgumentList '/c', ('taskkill /PID {0} /T /F >nul 2>&1' -f $proc.Id) -WindowStyle Hidden | Out-Null
    Start-Sleep -Milliseconds 500
}

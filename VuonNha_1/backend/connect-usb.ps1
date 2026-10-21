$ErrorActionPreference = 'Stop'
$scanAdbCandidates = @(
    "$env:ANDROID_HOME/platform-tools/adb.exe",
    "$env:LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
)
$scanAdb = $scanAdbCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
if (-not $scanAdb) {
    Write-Host 'ADB not found. Run adb reverse tcp:8787 tcp:8787 in Android Studio Terminal.'
    exit 1
}
$scanDevices = & $scanAdb devices
foreach ($scanLine in $scanDevices) {
    if ($scanLine -match '^(\S+)\s+device$') {
        & $scanAdb -s $Matches[1] reverse tcp:8787 tcp:8787
    }
}
Write-Host 'USB backend forwarding configured for connected devices.'

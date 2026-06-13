# Uso: .\scripts\bump-version.ps1 [major|minor|patch]
# Exemplos:
#   .\scripts\bump-version.ps1 patch   => 1.1.0 -> 1.1.1 (bug fix)
#   .\scripts\bump-version.ps1 minor   => 1.1.0 -> 1.2.0 (nova feature/milestone)
#   .\scripts\bump-version.ps1 major   => 1.1.0 -> 2.0.0 (breaking change)

param(
    [ValidateSet("major","minor","patch")]
    [string]$Tipo = "patch"
)

$gradleFile = "$PSScriptRoot\..\android\app\build.gradle.kts"

if (-not (Test-Path $gradleFile)) {
    Write-Error "Arquivo nao encontrado: $gradleFile"
    exit 1
}

$content = Get-Content $gradleFile -Raw

if ($content -notmatch 'versionCode\s*=\s*(\d+)') {
    Write-Error "versionCode nao encontrado no build.gradle.kts"
    exit 1
}
$versionCode = [int]$Matches[1]

if ($content -notmatch 'versionName\s*=\s*"(\d+)\.(\d+)\.(\d+)"') {
    Write-Error "versionName nao encontrado no build.gradle.kts (esperado: X.Y.Z)"
    exit 1
}
$major = [int]$Matches[1]
$minor = [int]$Matches[2]
$patch = [int]$Matches[3]

switch ($Tipo) {
    "major" { $major++; $minor = 0; $patch = 0 }
    "minor" { $minor++; $patch = 0 }
    "patch" { $patch++ }
}
$novoCode = $versionCode + 1
$novoName = "$major.$minor.$patch"

$newContent = $content `
    -replace "versionCode\s*=\s*\d+", "versionCode = $novoCode" `
    -replace 'versionName\s*=\s*"\d+\.\d+\.\d+"', "versionName = `"$novoName`""

Set-Content $gradleFile $newContent -Encoding UTF8 -NoNewline

Write-Host "Versao atualizada: v$novoName (code $novoCode)" -ForegroundColor Green

# Copiar APK para o Google Drive se existir
$apkOrigem = "$PSScriptRoot\..\android\app\build\outputs\apk\debug\app-debug.apk"
$driveDestino = "E:\google driver"
if ((Test-Path $apkOrigem) -and (Test-Path $driveDestino)) {
    $nomeApk = "mira-v$novoName.apk"
    Copy-Item $apkOrigem -Destination "$driveDestino\$nomeApk" -Force
    Write-Host "APK enviado para o Drive: $nomeApk" -ForegroundColor Cyan
} else {
    Write-Host "APK nao encontrado (rode assembleDebug antes de bump)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Proximo passo:"
Write-Host "  git add android/app/build.gradle.kts"
Write-Host "  git commit -m 'chore: bump para v$novoName'"

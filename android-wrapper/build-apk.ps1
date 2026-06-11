$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$AndroidDir = $PSScriptRoot
$Sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$BuildTools = Join-Path $Sdk "build-tools\35.0.0"
$AndroidJar = Join-Path $Sdk "platforms\android-35\android.jar"
$Aapt2 = Join-Path $BuildTools "aapt2.exe"
$D8 = Join-Path $BuildTools "d8.bat"
$ZipAlign = Join-Path $BuildTools "zipalign.exe"
$ApkSigner = Join-Path $BuildTools "apksigner.bat"
$KeyTool = "C:\Program Files\Java\jdk-17\bin\keytool.exe"

foreach ($path in @($AndroidJar, $Aapt2, $D8, $ZipAlign, $ApkSigner, $KeyTool)) {
  if (!(Test-Path -LiteralPath $path)) {
    throw "Ferramenta Android ausente: $path"
  }
}

$Build = Join-Path $AndroidDir "build"
$AssetsWeb = Join-Path $Build "assets\web"
$Classes = Join-Path $Build "classes"
$Dex = Join-Path $Build "dex"
$CompiledRes = Join-Path $Build "res.zip"
$UnsignedApk = Join-Path $Build "mira-unsigned.apk"
$DexApk = Join-Path $Build "mira-dex.apk"
$AlignedApk = Join-Path $Build "mira-aligned.apk"
$FinalApk = Join-Path $Build "Mira.apk"
$KeyStore = Join-Path $AndroidDir "mira-debug.keystore"

if (Test-Path -LiteralPath $Build) {
  Remove-Item -LiteralPath $Build -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $AssetsWeb, $Classes, $Dex | Out-Null

foreach ($file in @("index.html", "styles.css", "manifest.webmanifest", "sw.js")) {
  Copy-Item -LiteralPath (Join-Path $Root $file) -Destination $AssetsWeb
}
Copy-Item -LiteralPath (Join-Path $Root "src") -Destination (Join-Path $AssetsWeb "src") -Recurse
Copy-Item -LiteralPath (Join-Path $Root "assets") -Destination (Join-Path $AssetsWeb "assets") -Recurse

& $Aapt2 compile --dir (Join-Path $AndroidDir "res") -o $CompiledRes
& $Aapt2 link `
  -I $AndroidJar `
  --manifest (Join-Path $AndroidDir "AndroidManifest.xml") `
  -A (Join-Path $Build "assets") `
  -R $CompiledRes `
  --auto-add-overlay `
  --min-sdk-version 23 `
  --target-sdk-version 35 `
  --version-code 1 `
  --version-name "0.1.0" `
  -o $UnsignedApk

$JavaFiles = Get-ChildItem -LiteralPath (Join-Path $AndroidDir "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -source 8 -target 8 -classpath $AndroidJar -d $Classes $JavaFiles
$ClassFiles = Get-ChildItem -LiteralPath $Classes -Recurse -Filter *.class | ForEach-Object { $_.FullName }
& $D8 --lib $AndroidJar --output $Dex $ClassFiles

Copy-Item -LiteralPath $UnsignedApk -Destination $DexApk
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$Zip = [System.IO.Compression.ZipFile]::Open($DexApk, [System.IO.Compression.ZipArchiveMode]::Update)
try {
  $ExistingDex = $Zip.GetEntry("classes.dex")
  if ($ExistingDex) { $ExistingDex.Delete() }
  [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
    $Zip,
    (Join-Path $Dex "classes.dex"),
    "classes.dex"
  ) | Out-Null
} finally {
  $Zip.Dispose()
}

if (!(Test-Path -LiteralPath $KeyStore)) {
  & $KeyTool -genkeypair `
    -keystore $KeyStore `
    -storepass android `
    -keypass android `
    -alias mira `
    -keyalg RSA `
    -keysize 2048 `
    -validity 10000 `
    -dname "CN=Mira,O=Pata3D,C=BR"
}

& $ZipAlign -f -p 4 $DexApk $AlignedApk
& $ApkSigner sign `
  --ks $KeyStore `
  --ks-pass pass:android `
  --key-pass pass:android `
  --out $FinalApk `
  $AlignedApk
& $ApkSigner verify --print-certs $FinalApk

Write-Host "APK gerado em: $FinalApk"

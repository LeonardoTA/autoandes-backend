param([ValidateSet('run','test','package')][string]$Accion = 'run')
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$privateConfig = Join-Path (Split-Path $PSScriptRoot -Parent) '.local-autoandes/entorno.json'
if (Test-Path -LiteralPath $privateConfig) {
  $settings = Get-Content -LiteralPath $privateConfig -Raw | ConvertFrom-Json
  foreach ($key in @('DB_URL','DB_USERNAME','DB_PASSWORD','JWT_SECRET','JAVA_HOME')) {
    if ($settings.$key -and -not [Environment]::GetEnvironmentVariable($key, 'Process')) {
      [Environment]::SetEnvironmentVariable($key, [string]$settings.$key, 'Process')
    }
  }
}
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin/java.exe'))) { throw 'Configura JAVA_HOME con un JDK 21; consulta README.md.' }
$env:Path = (Join-Path $env:JAVA_HOME 'bin') + [IO.Path]::PathSeparator + $env:Path
foreach ($key in @('DB_PASSWORD','JWT_SECRET')) {
  if (-not [Environment]::GetEnvironmentVariable($key, 'Process')) { throw "Configura $key; consulta README.md." }
}
switch ($Accion) {
  'run' { & ./mvnw.cmd spring-boot:run }
  'test' { & ./mvnw.cmd test }
  'package' { & ./mvnw.cmd package }
}
exit $LASTEXITCODE

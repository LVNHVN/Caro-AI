# Chay game Caro AI (Gomoku) - JDK 23 + JavaFX 25
$ErrorActionPreference = "Stop"
$proj = $PSScriptRoot
$jdk  = "C:\Users\NC\tools\jdk-23.0.2+7"
$jfx  = "C:\Users\NC\tools\javafx-sdk-25.0.3\lib"

# Bien dich
$src = Get-ChildItem -Path "$proj\src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Path "$proj\bin" -Force | Out-Null
& "$jdk\bin\javac.exe" --module-path $jfx --add-modules javafx.controls,javafx.fxml -d "$proj\bin" $src
if ($LASTEXITCODE -ne 0) { Write-Error "Bien dich that bai"; exit 1 }

# Chay
& "$jdk\bin\java.exe" --module-path $jfx --add-modules javafx.controls,javafx.fxml -cp "$proj\bin" app.GomokuMain

@echo off
setlocal
pushd %~dp0
set LOGDIR=%~dp0logs
if not exist "%LOGDIR%" mkdir "%LOGDIR%"
set TS=%date:~-4%%date:~4,2%%date:~7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TS=%TS: =0%
set SERVER_OUT=%LOGDIR%\server_%TS%.out.log
set SERVER_ERR=%LOGDIR%\server_%TS%.err.log
set CLIENT_OUT=%LOGDIR%\client_%TS%.out.log
set CLIENT_ERR=%LOGDIR%\client_%TS%.err.log
set SERVER_LOG=%LOGDIR%\server_%TS%.app.log
set CLIENT_LOG=%LOGDIR%\client_%TS%.app.log
set GAMEJAR=%~dp0game\desktop-1.0.jar
set SERVER_EVAL_PORT=8791
set CLIENT_EVAL_PORT=8792

set LIMIT_SECS=%~1
if "%LIMIT_SECS%"=="" (
  echo Full test: starting server+client with no time limit...
) else (
  echo Full test: starting server+client for %LIMIT_SECS%s...
)
echo Server logs:
echo   %SERVER_OUT%
echo   %SERVER_ERR%
echo   %SERVER_LOG%
echo Client logs:
echo   %CLIENT_OUT%
echo   %CLIENT_ERR%
echo   %CLIENT_LOG%
echo Eval ports:
echo   server: %SERVER_EVAL_PORT%
echo   client: %CLIENT_EVAL_PORT%

if "%LIMIT_SECS%"=="" (
  powershell -Command ^
    "$ErrorActionPreference='SilentlyContinue';" ^
    "$srv=Start-Process -FilePath java -ArgumentList '-Dmewnbase.startServer=7777','-Dmewnbase.save=dawd','-Dmewnbase.eval=1','-Dmewnbase.eval.port=%SERVER_EVAL_PORT%','-Dmewnbase.logfile=%SERVER_LOG%','-jar','%GAMEJAR%' -RedirectStandardOutput '%SERVER_OUT%' -RedirectStandardError '%SERVER_ERR%' -PassThru;" ^
    "Start-Sleep -Seconds 2;" ^
    "$cli=Start-Process -FilePath java -ArgumentList '-Dmewnbase.connect=127.0.0.1:7777','-Dmewnbase.mp.nick=asus2','-Dmewnbase.mp.autoconnect=1','-Dmewnbase.eval=1','-Dmewnbase.eval.port=%CLIENT_EVAL_PORT%','-Dmewnbase.logfile=%CLIENT_LOG%','-jar','%GAMEJAR%' -RedirectStandardOutput '%CLIENT_OUT%' -RedirectStandardError '%CLIENT_ERR%' -PassThru;" ^
    "$serverReady=$false; $clientReady=$false; $deadline=(Get-Date).AddSeconds(30);" ^
    "while ((Get-Date) -lt $deadline) { " ^
      "if (-not $serverReady -and (Test-Path '%SERVER_LOG%')) { if (Select-String -Path '%SERVER_LOG%' -Pattern 'Server started on port' -Quiet) { $serverReady=$true } };" ^
      "if (-not $clientReady -and (Test-Path '%CLIENT_LOG%')) { if (Select-String -Path '%CLIENT_LOG%' -Pattern 'Started multiplayer client' -Quiet) { $clientReady=$true } };" ^
      "if ($serverReady -and $clientReady) { Write-Host 'READY FOR EVAL'; break };" ^
      "Start-Sleep -Milliseconds 500;" ^
    "};" ^
    "if (-not ($serverReady -and $clientReady)) { Write-Host 'READY TIMED OUT (check logs)'; }"
) else (
  powershell -Command ^
    "$ErrorActionPreference='SilentlyContinue';" ^
    "$srv=Start-Process -FilePath java -ArgumentList '-Dmewnbase.startServer=7777','-Dmewnbase.save=dawd','-Dmewnbase.eval=1','-Dmewnbase.eval.port=%SERVER_EVAL_PORT%','-Dmewnbase.logfile=%SERVER_LOG%','-jar','%GAMEJAR%' -RedirectStandardOutput '%SERVER_OUT%' -RedirectStandardError '%SERVER_ERR%' -PassThru;" ^
    "Start-Sleep -Seconds 2;" ^
    "$cli=Start-Process -FilePath java -ArgumentList '-Dmewnbase.connect=127.0.0.1:7777','-Dmewnbase.mp.nick=asus2','-Dmewnbase.mp.autoconnect=1','-Dmewnbase.eval=1','-Dmewnbase.eval.port=%CLIENT_EVAL_PORT%','-Dmewnbase.logfile=%CLIENT_LOG%','-jar','%GAMEJAR%' -RedirectStandardOutput '%CLIENT_OUT%' -RedirectStandardError '%CLIENT_ERR%' -PassThru;" ^
    "$serverReady=$false; $clientReady=$false; $deadline=(Get-Date).AddSeconds(30);" ^
    "while ((Get-Date) -lt $deadline) { " ^
      "if (-not $serverReady -and (Test-Path '%SERVER_LOG%')) { if (Select-String -Path '%SERVER_LOG%' -Pattern 'Server started on port' -Quiet) { $serverReady=$true } };" ^
      "if (-not $clientReady -and (Test-Path '%CLIENT_LOG%')) { if (Select-String -Path '%CLIENT_LOG%' -Pattern 'Started multiplayer client' -Quiet) { $clientReady=$true } };" ^
      "if ($serverReady -and $clientReady) { Write-Host 'READY FOR EVAL'; break };" ^
      "Start-Sleep -Milliseconds 500;" ^
    "};" ^
    "if (-not ($serverReady -and $clientReady)) { Write-Host 'READY TIMED OUT (check logs)'; }" ^
    "Start-Sleep -Seconds %LIMIT_SECS%;" ^
    "$srv,$cli | ForEach-Object { try { Stop-Process -Id $_.Id -Force } catch {} };" ^
    "Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like '*desktop-1.0.jar*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force };" ^
    "Get-CimInstance Win32_Process | Where-Object { $_.Name -ieq 'cmd.exe' } | Where-Object { $_.CommandLine -like '*run_server.bat*' -or $_.CommandLine -like '*run_client.bat*' -or $_.CommandLine -like '*run_both.bat*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }"
)

echo Done. Logs saved to:
echo   %SERVER_OUT%
echo   %SERVER_ERR%
echo   %SERVER_LOG%
echo   %CLIENT_OUT%
echo   %CLIENT_ERR%
echo   %CLIENT_LOG%
endlocal
popd

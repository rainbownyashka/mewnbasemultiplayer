@echo off
setlocal
pushd %~dp0
powershell -ExecutionPolicy Bypass -File "%~dp0fulltest.ps1" %*
popd
endlocal

@echo off
cd /d "%~dp0"
if not exist .env (
  echo Missing .env. See README.md to configure the backend.
  pause
  exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0connect-usb.ps1"
node --env-file=.env server.mjs
pause

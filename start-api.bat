@echo off
REM XAMPP API Auto-Startup Script
REM This script launches the Node.js API server connected to XAMPP MySQL
REM Run this after starting XAMPP

echo.
echo ========================================
echo  DOST XAMPP API Server Startup
echo ========================================
echo.

REM Check if Node.js is installed
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js from https://nodejs.org/
    echo.
    pause
    exit /b 1
)

REM Check if npm dependencies are installed
if not exist node_modules (
    echo Installing dependencies...
    call npm install
    if errorlevel 1 (
        echo ERROR: Failed to install dependencies
        pause
        exit /b 1
    )
)

echo.
echo ========================================
echo  Starting Node.js API Server
echo ========================================
echo.
echo API Server will run on: http://localhost:3000
echo MySQL Connection: localhost:3306
echo.
echo Press Ctrl+C to stop the server
echo.

REM Set environment variables for XAMPP MySQL
set DB_HOST=localhost
set DB_USER=root
set DB_PASSWORD=
set DB_NAME=dost_news

REM Start the API server
npm start

pause

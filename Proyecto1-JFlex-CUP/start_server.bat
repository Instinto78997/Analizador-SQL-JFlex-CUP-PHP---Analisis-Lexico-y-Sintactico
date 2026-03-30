@echo off
title Servidor SQL Analyzer - JFlex + CUP + PHP
color 0A

echo ========================================
echo    SERVIDOR SQL ANALYZER
echo    JFlex + CUP + PHP
echo ========================================
echo.

echo Verificando compilacion...
cd src

if not exist "Lexer.java" (
    echo [ERROR] Lexer.java no encontrado
    echo Ejecute build.bat primero
    cd ..
    pause
    exit /b 1
)

if not exist "parser.java" (
    echo [ERROR] parser.java no encontrado
    echo Ejecute build.bat primero
    cd ..
    pause
    exit /b 1
)

echo [OK] Archivos compilados encontrados
echo.

echo Iniciando servidor PHP...
echo.
echo Servidor disponible en: http://localhost:8080
echo Para detener el servidor: Ctrl+C
echo.

REM Iniciar servidor PHP en el puerto 8080
php -S localhost:8080 -t .

pause
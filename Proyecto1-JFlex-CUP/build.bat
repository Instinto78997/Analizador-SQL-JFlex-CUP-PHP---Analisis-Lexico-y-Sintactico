@echo off
title Compilador SQL Analyzer - JFlex y CUP
color 0A

echo ========================================
echo    COMPILADOR SQL ANALYZER
echo    JFlex + CUP + PHP
echo ========================================
echo.

cd src

echo Limpiando archivos anteriores...
del *.class 2>nul
del Lexer.java 2>nul
del parser.java 2>nul
del sym.java 2>nul
echo.

set JFLEX=..\tools\jflex-full-1.9.1.jar
set CUP=..\tools\java-cup-11b.jar
set CLASSPATH=.;%JFLEX%;%CUP%

echo [1/4] Generando Lexer.java con JFlex...
java -cp %JFLEX% jflex.Main lexer.flex
if errorlevel 1 (
    echo [ERROR] Fallo en JFlex
    pause
    exit /b 1
)
echo [OK] Lexer.java generado correctamente
echo.

echo [2/4] Generando parser.java y sym.java con CUP...
java -cp %CUP% java_cup.Main -parser parser -symbols sym -interface parser.cup
if errorlevel 1 (
    echo [ERROR] Fallo en CUP
    pause
    exit /b 1
)
echo [OK] parser.java y sym.java generados correctamente
echo.

echo [3/4] Compilando archivos Java...
javac -cp %CLASSPATH% *.java
if errorlevel 1 (
    echo [ERROR] Fallo en compilacion Java
    echo.
    echo Mostrando primeras lineas de Lexer.java para depuracion:
    echo ----------------------------------------
    head -n 20 Lexer.java 2>nul || type Lexer.java | more
    echo ----------------------------------------
    pause
    exit /b 1
)
echo [OK] Archivos Java compilados correctamente
echo.

cd ..

echo [4/4] Verificando estructura de archivos...
if exist "src\index.html" (
    echo [OK] index.html encontrado
) else (
    echo [WARN] index.html no encontrado
)

if exist "src\styles.css" (
    echo [OK] styles.css encontrado
) else (
    echo [WARN] styles.css no encontrado
)

if exist "src\script.js" (
    echo [OK] script.js encontrado
) else (
    echo [WARN] script.js no encontrado
)

if exist "src\analizar.php" (
    echo [OK] analizar.php encontrado
) else (
    echo [WARN] analizar.php no encontrado
)

echo.
echo ========================================
echo    COMPILACION COMPLETADA CON EXITO
echo ========================================
echo.
echo Para iniciar el servidor ejecute:
echo   start_server.bat
echo.
echo O directamente:
echo   php -S localhost:8080 -t src
echo.

pause
@echo off
title H2 Console - Sistema de Reservas
cd /d "%~dp0"

if not exist "dados\reservas.mv.db" (
    echo Banco H2 nao encontrado!
    echo Execute o sistema de reservas primeiro.
    pause
    exit /b
)

for /r "%USERPROFILE%\.m2\repository\com\h2database\h2" %%F in (h2-*.jar) do (
    set "H2_JAR=%%F"
)

if not defined H2_JAR (
    echo Biblioteca H2 nao encontrada!
    echo Execute o Maven para baixar as dependencias.
    pause
    exit /b
)

java -jar "%H2_JAR%"
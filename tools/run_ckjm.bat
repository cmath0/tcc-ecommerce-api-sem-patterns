@echo off
setlocal enabledelayedexpansion

REM Caminho base das classes compiladas (pasta onde começa a árvore a percorrer)
set BASE_PATH=C:\repositorio\ecommerce-api-sem-patterns\target\classes\com\github\cmath0

REM Caminho do JAR da ferramenta CKJM e da pasta target\classes para o classpath
set CKJM_JAR=C:\repositorio\ecommerce-api-sem-patterns\tools\ckjm_ext.jar
set TARGET_CLASSES=C:\repositorio\ecommerce-api-sem-patterns\target\classes

REM Pasta onde será criado o arquivo de saída (ajuste se desejar outro local)
set OUTPUT_DIR=C:\repositorio\ecommerce-api-sem-patterns\tools

REM Obter timestamp HHMMSS (tratando hora com espaço à esquerda)
set hh=%time:~0,2%
set hh=%hh: =0%
set mm=%time:~3,2%
set ss=%time:~6,2%
set TIMESTAMP=%hh%%mm%%ss%

set OUTPUT_FILE=%OUTPUT_DIR%\ck_metrics_%TIMESTAMP%.xml

echo Criando arquivo de saída: %OUTPUT_FILE%
rem cria (ou sobrescreve) o arquivo de saída vazio
break > "%OUTPUT_FILE%"

echo Executando MetricsFilter para cada pasta que contenha arquivos .class em:
echo %BASE_PATH%
echo.

echo ^<metrics^> >> "%OUTPUT_FILE%"

REM Percorre recursivamente todas as subpastas a partir de BASE_PATH
for /d /r "%BASE_PATH%" %%D in (*) do (
    REM Verifica se a pasta atual possui arquivos .class
    if exist "%%D\*.class" (
        echo Analisando pasta: %%D
        REM echo ----- Analisando pasta: %%D ----- >> "%OUTPUT_FILE%"
        java -cp "%CKJM_JAR%;%TARGET_CLASSES%" gr.spinellis.ckjm.MetricsFilter -x "%%D\*.class" >> "%OUTPUT_FILE%" 2>&1
        echo. >> "%OUTPUT_FILE%"
    )
)

echo ^</metrics^> >> "%OUTPUT_FILE%"

echo.
echo ====== Finalizado! ======
echo Resultado salvo em: %OUTPUT_FILE%
pause

@echo off
setlocal EnableDelayedExpansion

cd /d "%~dp0"

echo ==============================================================
echo        BELEZURA - ASSISTENTE DE DEPLOY SEGURO PARA GITHUB
echo ==============================================================
echo.

git --version >nul 2>&1
if errorlevel 1 (
    echo [ERRO] O Git nao foi encontrado no sistema.
    pause
    exit /b 1
)

for /f "delims=" %%A in ('git config user.name 2^>nul') do set "GIT_USER=%%A"
for /f "delims=" %%A in ('git config user.email 2^>nul') do set "GIT_EMAIL=%%A"
if not defined GIT_USER (
    echo [ERRO] Git sem nome configurado. Rode no terminal:
    echo     git config --global user.name "Seu Nome"
    echo     git config --global user.email "seuemail@exemplo.com"
    pause
    exit /b 1
)
if not defined GIT_EMAIL (
    echo [ERRO] Git sem email configurado. Rode no terminal:
    echo     git config --global user.email "seuemail@exemplo.com"
    pause
    exit /b 1
)

if not exist ".git" (
    echo [*] Inicializando novo repositorio Git...
    git init
)

git remote | findstr /i /r "^origin$" >nul 2>&1
if errorlevel 1 (
    echo [*] Adicionando remote origin...
    git remote add origin https://github.com/piecarlos/belezura.git
) else (
    echo [*] Garantindo URL do remote origin...
    git remote set-url origin https://github.com/piecarlos/belezura.git
)

echo [*] Indexando arquivos respeitando .gitignore...
git add -A

set "LEAK_FOUND=0"
for /f "delims=" %%F in ('git diff --cached --name-only') do (
    echo(%%F| findstr /i /r "keystore\.properties RELEASE_GUIDE\.md local\.properties \.jks$ \.keystore$ \.apk$ \.aab$ Backup_ modelo\.jpg" >nul
    if not errorlevel 1 (
        echo [ALERTA DE SEGURANCA] Arquivo sensivel detectado: %%F
        set "LEAK_FOUND=1"
    )
)

if "!LEAK_FOUND!"=="1" (
    echo.
    echo [BLOQUEIO CRITICO] Envio cancelado por seguranca!
    git reset >nul
    pause
    exit /b 1
)

echo.
echo Status atual:
echo --------------------------------------------------------------
git status -s
echo --------------------------------------------------------------
echo.

git diff --cached --quiet
if errorlevel 1 (
    set "DEFAULT_MSG=Atualizacao Belezura"
    set "COMMIT_MSG="
    set /p "COMMIT_MSG=Mensagem do commit (Enter para '!DEFAULT_MSG!'): "
    if not defined COMMIT_MSG set "COMMIT_MSG=!DEFAULT_MSG!"

    echo.
    echo [*] Criando commit...
    git commit -m "!COMMIT_MSG!"
    if errorlevel 1 (
        echo [ERRO] Falha ao criar o commit. Veja a mensagem acima.
        pause
        exit /b 1
    )
) else (
    echo [*] Nenhuma alteracao nova para comitar. Verificando push pendente...
)

git branch -M main

echo.
echo [*] Enviando para https://github.com/piecarlos/belezura.git (main)...
git push -u origin main
if errorlevel 1 (
    echo.
    echo ==============================================================
    echo [AVISO] O push falhou. Veja o erro detalhado acima.
    echo Se o GitHub ja tiver um README inicial, rode no terminal:
    echo     git pull --rebase origin main --allow-unrelated-histories
    echo E depois execute este script novamente.
    echo ==============================================================
) else (
    echo.
    echo ==============================================================
    echo [SUCESSO] Repositorio Belezura enviado com sucesso!
    echo ==============================================================
)

pause
# APK Android do Mira

O APK fica em:

```text
android-wrapper/build/Mira.apk
```

Para gerar novamente:

```powershell
powershell -ExecutionPolicy Bypass -File .\android-wrapper\build-apk.ps1
```

O app Android abre o Mira em uma WebView fullscreen, sem Chrome e sem barra de
endereço. Os arquivos web atuais são empacotados dentro do APK.

## Instalação no Xiaomi/MIUI

Se `adb install` retornar:

```text
INSTALL_FAILED_USER_RESTRICTED: Install canceled by user
```

ative no celular:

- Opções do desenvolvedor;
- Depuração USB;
- Instalar via USB;
- Depuração USB (configurações de segurança), se existir.

Depois rode:

```powershell
adb install -r .\android-wrapper\build\Mira.apk
```

Também há uma cópia prática em `C:\Users\maiko\Downloads\Mira.apk` quando a build
é gerada nesta máquina.

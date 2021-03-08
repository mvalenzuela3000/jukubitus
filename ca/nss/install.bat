@echo off
SET dirPF=
FOR /D %%P IN ("%appdata%\Mozilla\Firefox\Profiles\*") DO (
  IF EXIST "%%P\cert9.db" (
    SET dirPF=%%P
    BREAK
  )
)
IF "%dirPF%"=="" (
  echo Firefox no encontrado.
  EXIT
)
IF "%1"=="" (
  "%~dp0bin\certutil.exe" -L -d sql:"%dirPF%"
) ELSE (
  IF EXIST "%1" (
    "%~dp0bin\certutil.exe" -A -n adsib.gob.bo -i %1 -t "cTC,cTC,cTC" -d sql:"%dirPF%"
  ) ELSE (
    "%~dp0bin\certutil.exe" -D -n adsib.gob.bo -d sql:"%dirPF%"
  )
)
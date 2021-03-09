@echo off
SET dirL=%~dp0
IF "%dirL:~-4%"=="bin\" (
  cd ..
  cd ..
)
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
SET cert=%~1
IF "%cert%"=="" (
  "%~dp0bin\certutil.exe" -L -d sql:"%dirPF%"
) ELSE (
  IF "%cert%"=="delete" (
    "%~dp0bin\certutil.exe" -D -n adsib.gob.bo -d sql:"%dirPF%"
  ) ELSE (
    "%~dp0bin\certutil.exe" -A -n adsib.gob.bo -i "%cert%" -t "cTC,cTC,cTC" -d sql:"%dirPF%"
  )
)
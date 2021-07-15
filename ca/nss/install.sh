#!/bin/sh

FILES=$HOME/.mozilla/firefox/*

for f in $FILES; do
  if [ -f "$f/cert9.db" ]; then
    mozilla=$f
  fi
done

if [ -d "$mozilla" ]; then
  if [ -z "$1" ]; then
    certutil -L -d $mozilla
  else
    if [ "$1" = "delete" ]; then
      certutil -D -n adsib.gob.bo -d $mozilla
    else
      certutil -A -n adsib.gob.bo -i $1 -t "cTC,cTC,cTC" -d $mozilla
    fi
  fi
else
  echo "Firefox no encontrado."
fi

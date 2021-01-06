# Jacobitus Total
Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.



# Compilar el proyecto
Clonar el proyecto
```
$ git clone https://gitlab.softwarelibre.gob.bo/adsib/jacobitus4.git
```
Ingresar a la carpeta jacobitus4 y ejecutar
```
$ mvn clean package
```
Resultado
```
BUILD SUCCESS
```

# Instalador
## Windows
´´´
jpackage --input target --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type msi --icon iconos/icon.ico --win-menu
´´´

## Linux
´´´
jpackage --input target --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type deb --icon iconos/icon.png
´´´

## MacOS
´´´
/Library/Java/JavaVirtualMachines/jdk-14.0.2.jdk/Contents/Home/bin/jpackage --input target --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type pkg --icon iconos\icon.icns
´´´
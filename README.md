# Jacobitus Total
Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.

## Requerimientos
OpenJDK en sus versiones de 11 a 15

# Compilar el proyecto
Clonar el proyecto
```
$ git clone https://gitlab.softwarelibre.gob.bo/adsib/jacobitus4.git
```
Ingresar a la carpeta jacobitus4 y ejecutar
```
$ mvn clean
$ mvn package
```
Resultado
```
BUILD SUCCESS
```

# Generar documentación de los servicios REST
```
$ apidoc -i src/main/java/bo/firmadigital/jacobitus4/resources/ -o src/main/resources/web/apidoc
```

# Instalador
## Windows
```
jpackage --input target --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type msi --icon iconos/icon.ico -d dist --win-menu
```

## Linux
```
jpackage --input target --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type deb --icon iconos/icon.png -d dist
```

## MacOS
```
/Library/Java/JavaVirtualMachines/jdk-14.0.2.jdk/Contents/Home/bin/jpackage --input target --name "Jacobitus Total" --main-jar jacobitus4.jar --main-class bo.firmadigital.jacobitus4.Main --type pkg --icon iconos/icon.icns -d dist
```

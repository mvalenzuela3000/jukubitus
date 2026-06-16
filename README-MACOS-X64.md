# Jacobitus de Escritorio

Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.

## Requerimientos

- OpenJDK **15.0.2**
- Gradle 7.6.2
- clang++

### Instalación de Java 15

Descargamos el JDK desde el sitio oficial:

[OpenJDK Archive](https://jdk.java.net/archive/)

Instalamos utilidades necesarias:
```bash
$ sudo /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
$ sudo brew install wget tar
```

Descargamos OpenJDK 15.0.2:
```bash
$ sudo wget https://download.java.net/java/GA/jdk15.0.2/0d1cfde4252546c6931946de8db48ee2/7/GPL/openjdk-15.0.2_osx-x64_bin.tar.gz
```

Descomprimimos el archivo **openjdk-15.0.2_osx-x64_bin.tar.gz** en la carpeta **/Library/Java/JavaVirtualMachines/**:
```bash
$ sudo tar -zxf ./openjdk-15.0.2_osx-x64_bin.tar.gz -C /Library/Java/JavaVirtualMachines/
```

Verificamos la instalación:
```bash
$ java -version
```

### Instalación de Herramientas de línea de comandos de Xcode
```bash
$ xcode-select --install
```

Verificamos la instalación:
```bash
$ xcode-select -p
```

### Instalación clang++

El compilador viene con las Herramientas de línea de comandos de Xcode.

Verificamos la instalación:
```bash
$ clang++ --version
```

### Instalación git

El software de control de versiones viene con las Herramientas de línea de comandos de Xcode.

Verificamos la instalación:
```bash
$ git --version
```

Habilitamos el cache de credenciales:
```bash
$ git config --global credential.helper cache
```

## Compilación del proyecto

Clonamos el proyecto desde el Repositorio Estatal de Software Libre:
```bash
$ git clone https://gitlab.softwarelibre.gob.bo/adsib/jacobitus-total/jacobitus-escritorio.git --recursive
```

Ingresamos al directorio:
```bash
$ cd jacobitus-escritorio
```

### Ejecución de la aplicación

Compilamos:
```bash
$ ./gradlew clean jacobitus-libreria:buildNative escritorio:jar escritorio:copyAllDependencies escritorio:importCA escritorio:addDrivers escritorio:importChangePin
```

Ejecutamos:
```bash
$ ./gradlew escritorio:run
```

## Generación del paquete de instalación

Generamos:
```bash
$ ./gradlew packageApp
```

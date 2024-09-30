# Jacobitus Escritorio
Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.

## Requerimientos
- OpenJDK **15.0.2**
- Gradle 7.6.2
- g++ 10.x.x o superior

### Instalación de Java 15

Descargamos el JDK desde el sitio web https://jdk.java.net/archive/
```bash
$ sudo /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
$ sudo brew install wget

$ sudo wget https://download.java.net/java/GA/jdk15.0.2/0d1cfde4252546c6931946de8db48ee2/7/GPL/openjdk-15.0.2_osx-x64_bin.tar.gz
```

Descomprimimos el archivo **openjdk-15.0.2_osx-x64_bin.tar.gz** en la carpeta **/Library/Java/JavaVirtualMachines/**.
```bash
$ sudo brew install tar
$ sudo tar -zxf ./openjdk-15.0.2_osx-x64_bin.tar.gz -C /Library/Java/JavaVirtualMachines/
```

Verificamos la instalación.
```bash
$ java -version
```

### Instalación gradle

```bash
$ sudo apt update && sudo apt -y upgrade
$ sudo wget https://services.gradle.org/distributions/gradle-7.6.2-bin.zip
```

```bash
$ mkdir /opt/gradle
$ unzip -d /opt/gradle gradle-7.6.2-bin.zip
$ ls /opt/gradle/gradle-7.6.2
```

```bash
$ export PATH=$PATH:/opt/gradle/gradle-7.6.2/bin
```

Verificamos la instalación de Gradle.
```bash
$ gradle --version
```

### Instalación de Herramientas de línea de comandos de Xcode
```bash
$ xcode-select --install
```

Verificamos la instalación.
```bash
$ xcode-select -p
```

### Instalación g++

El compilador viene con las Herramientas de línea de comandos de Xcode.

Verificamos la instalación.
```bash
$ g++ --version
```

### Instalación git

El software de control de versiones viene con las Herramientas de línea de comandos de Xcode.

Verificamos la instalación.
```bash
$ git --version
```

Habilitamos el cache de credenciales para facilitar el manejo de submódulos
```bash
$ git config --global credential.helper cache
```

## Compilación del proyecto

Clonamos el proyecto desde el Repositorio Estatal de Software Libre.
```
$ git clone https://gitlab.softwarelibre.gob.bo/adsib/jacobitus-total/jacobitus-escritorio.git --recursive
```

### Gradle
Ingresamos a la carpeta **jacobitus-escritorio** y ejecutar los siguientes comandos:

```
$ ./gradlew jacobitus-libreria:buildForMacOS escritorio:clean escritorio:importCA escritorio:addDriversMacOS escritorio:importChangePinMacOS escritorio:shadowJar
$ ./gradlew escritorio:run
```

## Generar paquete de instalación

Para la generación de un archivo pkg es necesario ejecutar:
```
$ ./gradlew packagePkg
```

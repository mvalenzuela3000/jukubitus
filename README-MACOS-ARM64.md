# Jacobitus Escritorio
Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.

## Requerimientos
- OpenJDK **15.0.2**
- Gradle 7.6.2
- clang++

### Instalación de Java 15

Instalamos dependencias necesarias.
```bash
$ sudo /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
$ sudo brew install wget
```

Descargamos el JDK desde el sitio web https://bell-sw.com/pages/downloads/?version=java-15&os=macos&architecture=arm
```bash
$ sudo wget https://download.bell-sw.com/java/15.0.2+10/bellsoft-jdk15.0.2+10-macos-aarch64-full.zip
```

Descomprimimos el archivo **openjdk-15.0.2_osx-x64_bin.tar.gz** en la carpeta **/Library/Java/JavaVirtualMachines/**.
```bash
$ sudo unzip ./bellsoft-jdk15.0.2+10-macos-aarch64-full.zip -d /Library/Java/JavaVirtualMachines/
```

Agregamos la versión de java al PATH.
```bash
$ nano ~/.zshrc
```

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-15.0.2-full.jdk
export PATH=$JAVA_HOME/bin:$PATH
```

```bash
$ source ~/.zshrc
```

Verificamos la instalación.
```bash
$ java -version
```

### Instalación de Herramientas de línea de comandos de Xcode
```bash
$ xcode-select --install
```

Verificamos la instalación.
```bash
$ xcode-select -p
```

### Instalación clang++

El compilador viene con las Herramientas de línea de comandos de Xcode.

Verificamos la instalación.
```bash
$ clang++ --version
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
```bash
$ git clone https://gitlab.softwarelibre.gob.bo/adsib/jacobitus-total/jacobitus-escritorio.git --recursive
```

### Ejecución de la aplicación
Ingresamos a la carpeta **jacobitus-escritorio** y ejecutar los siguientes comandos:

```bash
$ ./gradlew jacobitus-libreria:buildNative escritorio:clean escritorio:importCA escritorio:addDrivers escritorio:importChangePin escritorio:shadowJar
$ ./gradlew escritorio:run
```

## Generación del paquete de instalación

Para la generación de un archivo pkg es necesario ejecutar:
```bash
$ ./gradlew packageApp
```

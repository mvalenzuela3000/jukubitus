# Jacobitus Total (versión de escritorio)
Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.

## Requerimientos
- OpenJDK **15.0.2**
- Gradle 7.6.2
- g++ 10.x.x o superior
- ApiDoc 0.29

### Instalación de Java 15

Descargamos el JDK desde el sitio web https://jdk.java.net/archive/
```bash
$ sudo apt update && sudo apt -y upgrade
$ sudo apt -y install wget

$ sudo wget https://download.java.net/java/GA/jdk15.0.2/0d1cfde4252546c6931946de8db48ee2/7/GPL/openjdk-15.0.2_linux-x64_bin.tar.gz
```

Descomprimimos el archivo **openjdk-15.0.2_linux-x64_bin.tar.gz** en la carpeta **/usr/lib/jvm**.
```bash
$ sudo tar -zxf ./openjdk-15.0.2_linux-x64_bin.tar.gz -C /usr/lib/jvm
```

Registramos la versión de **java**.
```bash
$ sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-15.0.2/bin/java 1502
```

Registramos la versión de **java**.
```bash
$ sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/jdk-15.0.2/bin/javac 1502
```

Establecemos Java 15 como versión por defecto.
```bash
$ sudo update-alternatives --config java
```

Seleccionamos la versión que se encuentra en la ruta **/usr/lib/jvm/jdk-15.0.2/bin/java**.

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

### Instalación g++

```bash
$ sudo apt update && sudo apt -y upgrade
$ sudo apt -y install build-essential
```

Verificamos la instalación.
```bash
$ g++ --version
```

### Instalación git

```bash
$ sudo apt update && sudo apt -y upgrade
$ sudo apt -y install git
```

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
$ git clone https://gitlab.softwarelibre.gob.bo/adsib/jacobitus4.git --recursive
```

### Gradle
Ingresamos a la carpeta **jacobitus4** y ejecutar los siguientes comandos:

### Linux

```
$ ./gradlew libreria:buildForDebian clean importCA importChangePinDebian jar
$ ./gradlew run
```

### Windows

```
$ ./gradlew libreria:buildForWindows clean importCA importChangePinWindows jar
$ ./gradlew run
```

### MacOS

```
$ ./gradlew libreria:buildForMacOS clean importCA importChangePinMacOS jar
$ ./gradlew run
```

## Documentación de los servicios REST
Instalamos de manera global el paquete **apidoc**.
```
$ npm install -g apidoc@0.29
```

Generamos la documentación.
```
$ apidoc -i src/main/java/bo/firmadigital/jacobitus4/resources/ -o src/main/resources/web/apidoc
```

## Generar paquete de instalación

### Linux

Para la generación de un archivo deb es necesario:

- fakeroot para Debian/Ubuntu Linux.

Ejecutamos el siguiente comando:
```
$ ./gradlew packageDeb
```

<!-- Para la generación de un archivo rpm es necesario:

- rpm-build para Red Hat Linux.

Ejecutamos el siguiente comando:
```
$ ./gradlew packageRpm
``` -->

### Windows

Para la generación de un archivo deb/rpm es necesario:

- WiX 3.11.x

Para la generación de un archivo msi es necesario ejecutar:
```
$ ./gradlew packageMsi
```

### MacOS

Para la generación de un archivo dmg es necesario ejecutar:
```
$ ./gradlew packageDmg
```

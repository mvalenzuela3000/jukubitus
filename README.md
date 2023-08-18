# Jacobitus Total (versión de escritorio)
Es una aplicación desarrollada con JavaFX para firma y validación de firma digital en diferentes documentos.

## Requerimientos
- OpenJDK **15.0.2**
- Maven 3.x.x o Gradle 7.6.2
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
```
$ ./gradlew clean libreria:jar jar
```

## Documentación de los servicios REST
```
$ apidoc -i src/main/java/bo/firmadigital/jacobitus4/resources/ -o src/main/resources/web/apidoc
```

## Generar instalador

### Linux
Para la generación de un archivo deb/rpm es necesario:

- fakeroot para Debian/Ubuntu Linux.
- rpm-build para Red Hat Linux.

Ejecutamos el siguiente comando:
```
$ ./gradlew deb
```
```
$ ./gradlew rpm
```

### Windows
Para la generación de un archivo exe/msi es necesario:

- WiX 3.0 o posterior.

Ejecutamos el siguiente comando:
```
$ ./gradlew msi
```

### MacOS
Para la generación de un archivo pkg (dmg) es necesario:

- Las herramientas de línea de comandos de Xcode cuando se usa la opción --mac-sign para solicitar que se firme el paquete y cuando se usa la opción --icon para personalizar la imagen DMG.

Ejecutamos el siguiente comando:
```
$ ./gradlew dmg
```
